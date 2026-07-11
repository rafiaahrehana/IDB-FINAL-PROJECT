import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Client } from '../../models/crm.model';
import { ClientService } from '../../services/client.service';
import { EmployeeService } from '../../../hrm/services/employee.service';
import { Employee } from '../../../hrm/models/hrm.model';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

// Mirror of backend ClientStatus enum
const CLIENT_STATUSES = ['ACTIVE', 'INACTIVE', 'BLOCKED'] as const;

@Component({
  selector: 'app-clients',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './clients.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './clients.scss',
})
export class Clients implements OnInit {
  clients: Client[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';

  statuses = CLIENT_STATUSES;
  employees: Employee[] = [];

  // Create modal (backend CreateClientRequest provisions a portal user: email + password)
  showCreate = false;
  saving = false;
  createForm: any = this.emptyCreateForm();

  // Edit modal (backend UpdateClientRequest - account-level fields only)
  editing: Client | null = null;
  editForm: any = {};

  deleteTarget: Client | null = null;

  constructor(
    private clientService: ClientService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
    this.employeeService.list(0, 100).subscribe({ next: (res) => (this.employees = res.content) });
  }

  private emptyCreateForm(): any {
    return {
      firstName: '', lastName: '', email: '', password: '', phone: '',
      clientCompanyName: '', industry: '', website: '', taxId: '', accountManagerId: null,
    };
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.clientService.list(this.page, 20, this.statusFilter || undefined).subscribe({
      next: (res) => {
        this.clients = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load clients';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    this.createForm = this.emptyCreateForm();
    this.showCreate = true;
  }

  saveCreate(): void {
    const f = this.createForm;
    if (!f.firstName?.trim() || !f.lastName?.trim() || !f.email?.trim() || !f.password) {
      this.error = 'First name, last name, email and password are required';
      return;
    }
    this.saving = true;
    this.error = '';
    const payload: any = {};
    Object.entries(f).forEach(([k, v]) => { if (v !== '' && v !== null) payload[k] = v; });
    this.clientService.create(payload).subscribe({
      next: () => {
        this.success = 'Account created';
        this.saving = false;
        this.showCreate = false;
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to create account';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  openEdit(client: Client): void {
    this.editing = client;
    this.editForm = {
      clientCompanyName: client.clientCompanyName || '',
      industry: client.industry || '',
      website: client.website || '',
      status: client.status,
      accountManagerId: client.accountManagerId ?? null,
      portalAccessEnabled: client.portalAccessEnabled ?? false,
    };
  }

  saveEdit(): void {
    if (!this.editing) return;
    this.saving = true;
    this.error = '';
    this.clientService.update(this.editing.id, this.editForm).subscribe({
      next: () => {
        this.success = 'Account updated';
        this.saving = false;
        this.editing = null;
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update account';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.clientService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Account deleted';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete account';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  statusClass(status: string): string {
    return {
      ACTIVE: 'text-bg-success', INACTIVE: 'text-bg-secondary', BLOCKED: 'text-bg-danger',
    }[status] || 'text-bg-secondary';
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
