import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { PermissionService } from '../services/permission.service';

/**
 * The company dashboard (data cards, charts) is only useful to COMPANY_OWNER and to
 * custom roles the owner has granted every permission to - a plain EMPLOYEE role sees
 * mostly-empty/irrelevant cards for sections they have no access to. Everyone else
 * (platform staff, CLIENT) already has its own landing flow and is untouched here.
 */
@Injectable({ providedIn: 'root' })
export class DashboardAccessGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private permissionService: PermissionService,
    private router: Router,
  ) {}

  canActivate(): boolean {
    const roles = this.authService.getCurrentUser()?.roles ?? [];
    const isRestrictedEmployee = roles.includes('EMPLOYEE') && !roles.includes('COMPANY_OWNER');

    if (isRestrictedEmployee && !this.permissionService.hasAllPermissions()) {
      this.router.navigate(['/my-profile']);
      return false;
    }
    return true;
  }
}
