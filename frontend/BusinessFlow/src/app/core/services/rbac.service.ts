import { Injectable, signal, computed } from '@angular/core';
import { Role, PermissionCode, PlatformRole, TenantRole, PLATFORM_ROLES, TENANT_ROLES } from '../models/rbac.model';
import { EncryptionService } from './encryption.service';

@Injectable({ providedIn: 'root' })
export class RbacService {
  private readonly _currentRole = signal<Role | null>(null);
  private readonly _permissions = signal<PermissionCode[]>([]);
  private readonly _customRoleId = signal<number | null>(null);

  readonly currentRole = this._currentRole.asReadonly();
  readonly permissions = this._permissions.asReadonly();

  readonly isPlatformAdmin = computed(() => {
    const role = this._currentRole();
    return role != null && (PLATFORM_ROLES as string[]).includes(role);
  });

  readonly isCompanyOwner = computed(() => this._currentRole() === 'COMPANY_OWNER');
  readonly isClient = computed(() => this._currentRole() === 'CLIENT');
  readonly isEmployee = computed(() => this._currentRole() === 'EMPLOYEE');

  readonly isTenantUser = computed(() => {
    const role = this._currentRole();
    return role != null && (TENANT_ROLES as string[]).includes(role);
  });

  constructor(private encryption: EncryptionService) {
    this.loadFromStorage();
  }

  setRole(role: Role): void {
    this._currentRole.set(role);
  }

  setPermissions(permissions: PermissionCode[]): void {
    this._permissions.set(permissions);
  }

  setCustomRoleId(id: number | null): void {
    this._customRoleId.set(id);
  }

  hasRole(role: Role): boolean {
    return this._currentRole() === role;
  }

  hasAnyRole(roles: Role[]): boolean {
    const current = this._currentRole();
    return current != null && roles.includes(current);
  }

  hasPermission(permission: PermissionCode): boolean {
    const role = this._currentRole();
    if (!role) return false;
    if (role === 'COMPANY_OWNER') return true;
    if (role === 'SUPER_ADMIN' || role === 'SYSTEM_ADMIN') return true;
    return this._permissions().includes(permission);
  }

  hasAnyPermission(permissions: PermissionCode[]): boolean {
    return permissions.some(p => this.hasPermission(p));
  }

  canAccess(roles?: Role[], permissions?: PermissionCode[]): boolean {
    if (roles && roles.length > 0) {
      if (!this.hasAnyRole(roles)) return false;
    }
    if (permissions && permissions.length > 0) {
      if (!this.hasAnyPermission(permissions)) return false;
    }
    return true;
  }

  hasModuleAccess(module: string): boolean {
    const role = this._currentRole();
    if (!role) return false;
    if (role === 'SUPER_ADMIN' || role === 'SYSTEM_ADMIN') return true;
    if (role === 'COMPANY_OWNER') return true;

    const moduleAccess: Record<string, Role[]> = {
      crm: ['COMPANY_OWNER', 'EMPLOYEE'],
      hrm: ['COMPANY_OWNER', 'EMPLOYEE'],
      finance: ['COMPANY_OWNER', 'EMPLOYEE'],
      servicedesk: ['COMPANY_OWNER', 'EMPLOYEE', 'CLIENT'],
      support: ['COMPANY_OWNER', 'EMPLOYEE', 'SUPPORT_AGENT', 'SUPPORT_MANAGER'],
      itam: ['COMPANY_OWNER', 'EMPLOYEE'],
      attendance: ['COMPANY_OWNER', 'EMPLOYEE'],
      'platform-admin': ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'],
    };

    return moduleAccess[module]?.includes(role) ?? false;
  }

  private loadFromStorage(): void {
    try {
      const encryptedRole = sessionStorage.getItem('bos_role') || localStorage.getItem('bos_role');
      const encryptedPerms = sessionStorage.getItem('bos_permissions') || localStorage.getItem('bos_permissions');

      if (encryptedRole) {
        const role = this.encryption.decrypt(encryptedRole) as Role;
        if (role) this._currentRole.set(role);
      }

      if (encryptedPerms) {
        const permsStr = this.encryption.decrypt(encryptedPerms);
        if (permsStr) {
          const perms = JSON.parse(permsStr) as PermissionCode[];
          this._permissions.set(perms);
        }
      }
    } catch {
      this.clear();
    }
  }

  saveToStorage(role: Role, permissions: PermissionCode[], remember = false): void {
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem('bos_role', this.encryption.encrypt(role));
    storage.setItem('bos_permissions', this.encryption.encrypt(JSON.stringify(permissions)));
    this._currentRole.set(role);
    this._permissions.set(permissions);
  }

  clear(): void {
    this._currentRole.set(null);
    this._permissions.set([]);
    this._customRoleId.set(null);
    ['bos_role', 'bos_permissions'].forEach(key => {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    });
  }
}
