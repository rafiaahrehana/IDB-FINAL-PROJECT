import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
 
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  // Emits the new access token once a refresh completes, so requests that arrived
  // while a refresh was already in flight can wait for it instead of firing their own.
  private refreshedToken$ = new BehaviorSubject<string | null>(null);
 
  constructor(private authService: AuthService) {}
 
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip token for auth endpoints
    if (this.isAuthEndpoint(request.url)) {
      return next.handle(request);
    }
 
    // Add token to request
    const token = this.authService.getAccessToken();
    if (token) {
      request = this.addToken(request, token);
    }
 
    return next.handle(request).pipe(
      catchError(error => {
        if (error.status === 401) {
          return this.handle401(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private handle401(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.authService.getRefreshToken()) {
      this.authService.logout();
      return throwError(() => new Error('Session expired. Please log in again.'));
    }

    if (this.isRefreshing) {
      // A refresh is already in flight: wait for it to finish, then retry with the new token.
      return this.refreshedToken$.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => next.handle(this.addToken(request, token as string)))
      );
    }

    this.isRefreshing = true;
    this.refreshedToken$.next(null);

    return this.authService.refreshToken().pipe(
      switchMap(response => {
        this.isRefreshing = false;
        this.refreshedToken$.next(response.accessToken);
        return next.handle(this.addToken(request, response.accessToken));
      }),
      catchError(err => {
        this.isRefreshing = false;
        // /auth/refresh only ever fails with 400 (BadRequestException, for both an
        // invalid and an expired/revoked token) - never 401/403. A network failure
        // (status 0) or a 5xx just means the backend was briefly unreachable (e.g.
        // restarting during dev) - don't wipe a perfectly good session for that.
        if (err.status === 400 || err.status === 401 || err.status === 403) {
          this.authService.logout();
        }
        return throwError(() => err);
      })
    );
  }
 
  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
 
  private isAuthEndpoint(url: string): boolean {
    return url.includes('/auth/login') || url.includes('/auth/register') || url.includes('/auth/refresh');
  }
}