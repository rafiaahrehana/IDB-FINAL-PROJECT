import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../../shared/services/notification.service';
 
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate, CanActivateChild {
  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}
 
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.checkRole(route);
  }
 
  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.checkRole(route);
  }
 
  private checkRole(route: ActivatedRouteSnapshot): boolean {
    const requiredRoles = route.data['roles'] as string[];
 
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }
 
    if (this.authService.hasAnyRole(requiredRoles)) {
      return true;
    }
 
    this.notificationService.error('Insufficient permissions');
    this.router.navigate(['/forbidden']);
    return false;
  }
}
