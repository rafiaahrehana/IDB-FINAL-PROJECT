import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PLATFORM_ROLES, PlatformRole, PlatformUser, PlatformUserRequest } from '../../models/platform-admin.model';
import { PlatformUserService } from '../../services/platform-user.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-platform-users',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './platform-users.html',
})
export class PlatformUsers implements OnInit {
  users: PlatformUser[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: PlatformUserRequest = this.emptyForm();
  deactivateTarget: PlatformUser | null = null;

  roles: PlatformRole[] = PLATFORM_ROLES;

  constructor(private userService: PlatformUserService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  emptyForm(): PlatformUserRequest {
    return { firstName: '', lastName: '', email: '', password: '', role: 'SYSTEM_ADMIN', phone: '' };
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.userService.list(this.page).subscribe({
      next: (res) => {
        this.users = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load platform users';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  openEdit(u: PlatformUser): void {
    this.editingId = u.id;
    // Password left blank on edit - only sent to the backend (and only changed) if filled in
    this.form = { firstName: u.firstName, lastName: u.lastName, email: u.email, password: '', role: u.role, phone: u.phone };
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    this.success = '';
    this.cdr.markForCheck();
    const op = this.editingId
      ? this.userService.update(this.editingId, this.form)
      : this.userService.create(this.form);
    op.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Platform user updated' : `Platform user ${this.form.email} created`;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save platform user';
        this.cdr.markForCheck();
      },
    });
  }

  doDeactivate(): void {
    if (!this.deactivateTarget) return;
    this.userService.deactivate(this.deactivateTarget.id).subscribe({
      next: () => {
        this.deactivateTarget = null;
        this.success = 'Platform user deactivated';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.deactivateTarget = null;
        this.error = err?.error?.message || 'Failed to deactivate';
        this.cdr.markForCheck();
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  roleLabel(role: string): string {
    return role.replace(/_/g, ' ').toLowerCase();
  }
}
