import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, CanActivateChild } from '@angular/router';
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
 
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.checkAuth(state.url);
  }
 
  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.checkAuth(state.url);
  }
 
  private checkAuth(url: string): boolean {
    if (this.authService.isAuthenticated()) {
      return true;
    }

    // A visitor landing on the app root gets the public landing page;
    // deep links still go to login with a returnUrl.
    if (url === '/' || url === '') {
      this.router.navigate(['/home']);
      return false;
    }

    this.notificationService.warning('Please log in first');
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: url } });
    return false;
  }
}