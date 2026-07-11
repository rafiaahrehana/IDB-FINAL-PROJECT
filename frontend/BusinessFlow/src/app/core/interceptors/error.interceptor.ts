import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from '../../shared/services/notification.service';
 
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private notificationService: NotificationService) {}
 
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        const errorMessage = this.getErrorMessage(error);
        
        if (error.status !== 401) {
          this.notificationService.error(errorMessage);
        }
 
        return throwError(() => error);
      })
    );
  }
 
  private getErrorMessage(error: HttpErrorResponse): string {
    if (error.error instanceof ErrorEvent) {
      return error.error.message;
    }
 
    if (error.error && typeof error.error === 'object') {
      return error.error.error || error.error.message || 'An error occurred';
    }
 
    return error.message || 'An error occurred';
  }
}