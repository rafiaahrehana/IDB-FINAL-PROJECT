import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomRole, CustomRoleRequest } from '../../models/platform-admin.model';
import { CustomRoleService } from '../../services/custom-role.service';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

interface PermissionGroup {
  label: string;
  permissions: string[];
}

@Component({
  selector: 'app-custom-roles',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog],
  templateUrl: './custom-roles.html',
})
export class CustomRoles implements OnInit {
  roles: CustomRole[] = [];
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: CustomRoleRequest = { name: '' };

  // Permission management
  permissionGroups: PermissionGroup[] = [];
  selectedPermissions: Set<string> = new Set();
  permissionsLoading = false;
  savingPermissions = false;

  deleteTarget: CustomRole | null = null;

  private readonly PERMISSION_GROUPS: Record<string, string[]> = {
    'Company': ['COMPANY_VIEW', 'COMPANY_UPDATE', 'COMPANY_SETTINGS', 'COMPANY_BRANDING'],
    'Users': ['USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE'],
    'Employees': ['EMPLOYEE_VIEW', 'EMPLOYEE_CREATE', 'EMPLOYEE_UPDATE', 'EMPLOYEE_DELETE'],
    'Departments': ['DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_DELETE'],
    'Designations': ['DESIGNATION_VIEW', 'DESIGNATION_CREATE', 'DESIGNATION_UPDATE', 'DESIGNATION_DELETE'],
    'Leaves': ['LEAVE_VIEW', 'LEAVE_CREATE', 'LEAVE_UPDATE', 'LEAVE_CANCEL', 'LEAVE_APPROVE', 'LEAVE_REJECT'],
    'Expenses': ['EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_UPDATE', 'EXPENSE_DELETE', 'EXPENSE_APPROVE', 'EXPENSE_REJECT'],
    'Attendance': ['ATTENDANCE_VIEW', 'ATTENDANCE_MARK', 'ATTENDANCE_UPDATE'],
    'Payroll': ['PAYROLL_VIEW', 'PAYROLL_PROCESS', 'PAYROLL_APPROVE'],
    'Service Requests': ['SERVICE_REQUEST_VIEW', 'SERVICE_REQUEST_CREATE', 'SERVICE_REQUEST_ASSIGN', 'SERVICE_REQUEST_APPROVE', 'SERVICE_REQUEST_CLOSE'],
    'Clients': ['CLIENT_VIEW', 'CLIENT_CREATE', 'CLIENT_UPDATE', 'CLIENT_DELETE'],
    'AI': ['AI_CHAT', 'AI_ADMIN'],
  };

  constructor(private roleService: CustomRoleService) {}

  ngOnInit(): void {
    this.load();
    this.loadPermissionGroups();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.roleService.list().subscribe({
      next: (res) => { this.roles = res || []; this.loading = false; },
      error: () => { this.error = 'Failed to load custom roles'; this.loading = false; }
    });
  }

  loadPermissionGroups(): void {
    this.permissionGroups = Object.entries(this.PERMISSION_GROUPS).map(([label, permissions]) => ({
      label,
      permissions
    }));
  }

  openCreate(): void {
    this.editingId = null;
    this.form = { name: '' };
    this.selectedPermissions.clear();
    this.showForm = true;
  }

  openEdit(r: CustomRole): void {
    if (r.systemRole) return;
    this.editingId = r.id;
    this.form = { name: r.name, description: r.description };
    this.selectedPermissions.clear();
    this.loadRolePermissions(r.id);
    this.showForm = true;
  }

  loadRolePermissions(roleId: number): void {
    this.permissionsLoading = true;
    this.roleService.getPermissions(roleId).subscribe({
      next: (perms) => {
        this.selectedPermissions = new Set(perms);
        this.permissionsLoading = false;
      },
      error: () => { this.permissionsLoading = false; }
    });
  }

  save(): void {
    const op = this.editingId
      ? this.roleService.update(this.editingId, this.form)
      : this.roleService.create(this.form);
    op.subscribe({
      next: (created) => {
        this.success = this.editingId ? 'Role updated' : 'Role created';
        if (!this.editingId && created.id) {
          this.editingId = created.id;
        } else {
          this.showForm = false;
          this.editingId = null;
        }
        this.load();
      },
      error: (err) => this.error = err?.error?.message || 'Failed to save role'
    });
  }

  togglePermission(code: string): void {
    if (this.selectedPermissions.has(code)) {
      this.selectedPermissions.delete(code);
    } else {
      this.selectedPermissions.add(code);
    }
  }

  isPermissionSelected(code: string): boolean {
    return this.selectedPermissions.has(code);
  }

  toggleGroup(group: PermissionGroup): void {
    const allSelected = group.permissions.every(p => this.selectedPermissions.has(p));
    group.permissions.forEach(p => {
      if (allSelected) {
        this.selectedPermissions.delete(p);
      } else {
        this.selectedPermissions.add(p);
      }
    });
  }

  isGroupSelected(group: PermissionGroup): boolean {
    return group.permissions.every(p => this.selectedPermissions.has(p));
  }

  isGroupPartial(group: PermissionGroup): boolean {
    const count = group.permissions.filter(p => this.selectedPermissions.has(p)).length;
    return count > 0 && count < group.permissions.length;
  }

  savePermissions(): void {
    if (!this.editingId) return;
    this.savingPermissions = true;
    this.roleService.setPermissions(this.editingId, Array.from(this.selectedPermissions)).subscribe({
      next: () => {
        this.savingPermissions = false;
        this.success = 'Permissions saved';
        this.load();
      },
      error: () => {
        this.savingPermissions = false;
        this.error = 'Failed to save permissions';
      }
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.roleService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Role deleted'; this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete role'; }
    });
  }
}
