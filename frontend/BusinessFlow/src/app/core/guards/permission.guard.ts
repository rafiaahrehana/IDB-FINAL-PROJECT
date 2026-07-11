import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { RbacService } from '../services/rbac.service';
import { PermissionCode } from '../models/rbac.model';

export const permissionGuard: CanActivateFn = (route) => {
  const rbac = inject(RbacService);
  const router = inject(Router);

  const requiredPermissions = route.data?.['permissions'] as PermissionCode[] | undefined;

  if (!requiredPermissions || requiredPermissions.length === 0) {
    return true;
  }

  if (rbac.hasAnyPermission(requiredPermissions)) {
    return true;
  }

  router.navigate(['/forbidden']);
  return false;
};

export const moduleGuard: CanActivateFn = (route) => {
  const rbac = inject(RbacService);
  const router = inject(Router);

  const module = route.data?.['module'] as string | undefined;

  if (!module) return true;

  if (rbac.hasModuleAccess(module)) {
    return true;
  }

  router.navigate(['/forbidden']);
  return false;
};
