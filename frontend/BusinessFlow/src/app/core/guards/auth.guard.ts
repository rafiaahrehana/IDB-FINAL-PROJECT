import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, CanActivateChild } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../../shared/services/notification.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.checkAuth(state.url);
  }

  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.checkAuth(state.url);
  }

  private checkAuth(url: string): Observable<boolean> {
    if (this.authService.isAuthenticated()) {
      return of(true);
    }

    // The local access token (15 min TTL) may have simply expired while a valid
    // refresh token (7 day TTL) is still on hand - try a silent refresh before
    // forcing a hard logout on what would otherwise be ordinary navigation.
    if (this.authService.getRefreshToken()) {
      return this.authService.refreshToken().pipe(
        map(() => true),
        catchError((err) => {
          // /auth/refresh only ever fails with 400 (BadRequestException, for both an
          // invalid and an expired/revoked token) - never 401/403. A status of 0 or 5xx
          // means the backend was transiently unreachable, not that the token is bad.
          // Purge it now so it isn't retried (and re-fail the same way) on every
          // subsequent navigation. ErrorInterceptor already surfaced the specific reason.
          const serverRejected = err?.status === 400 || err?.status === 401 || err?.status === 403;
          if (serverRejected) {
            this.authService.clearSession();
          }
          return of(this.redirectToLogin(url, serverRejected));
        }),
      );
    }

    return of(this.redirectToLogin(url));
  }

  private redirectToLogin(url: string, skipNotification = false): boolean {
    // A visitor landing on the app root gets the public landing page;
    // deep links still go to login with a returnUrl.
    if (url === '/' || url === '') {
      this.router.navigate(['/home']);
      return false;
    }

    if (!skipNotification) {
      this.notificationService.warning('Please log in first');
    }
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: url } });
    return false;
  }
}