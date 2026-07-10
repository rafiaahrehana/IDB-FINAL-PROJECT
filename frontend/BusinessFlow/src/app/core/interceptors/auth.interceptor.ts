import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
 
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
 
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
        if (error.status === 401 && !this.isRefreshing) {
          this.isRefreshing = true;
          return this.authService.refreshToken().pipe(
            switchMap(response => {
              this.isRefreshing = false;
              const newToken = this.authService.getAccessToken()!;
              return next.handle(this.addToken(request, newToken));
            }),
            catchError(err => {
              this.isRefreshing = false;
              this.authService.logout();
              return throwError(() => err);
            })
          );
        }
        return throwError(() => error);
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
    return url.includes('/auth/login') || url.includes('/auth/register');
  }
}