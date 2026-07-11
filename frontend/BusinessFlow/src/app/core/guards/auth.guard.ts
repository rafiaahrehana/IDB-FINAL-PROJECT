import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, CanActivateChild } from '@angular/router';
import { Observable, of, switchMap, map, catchError } from 'rxjs';
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

    if (this.authService.getRefreshToken()) {
      return this.authService.refreshToken().pipe(
        map(() => true),
        catchError(() => {
          this.sendRedirect(url);
          return of(false);
        })
      );
    }

    this.sendRedirect(url);
    return of(false);
  }

  private sendRedirect(url: string): void {
    if (url === '/' || url === '') {
      this.router.navigate(['/home']);
      return;
    }
    this.notificationService.warning('Please log in first');
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: url } });
  }
}
