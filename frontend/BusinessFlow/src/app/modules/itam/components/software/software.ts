import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SoftwareLicense } from '../../models/itam.model';
import { SoftwareService } from '../../services/software.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-software',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './software.html',
})
export class Software implements OnInit {
  licenses: SoftwareLicense[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  showExpiring = false;
  showForm = false;
  editId: number | null = null;
  form: any = {};
  deleteTarget: SoftwareLicense | null = null;
  statuses = ['ACTIVE', 'EXPIRED', 'EXPIRING_SOON', 'CANCELLED'];
  licenseTypes = ['PERPETUAL', 'SUBSCRIPTION', 'VOLUME', 'OEM', 'FREEWARE', 'OPEN_SOURCE'];

  constructor(private softwareService: SoftwareService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    const obs = this.showExpiring
      ? this.softwareService.expiring()
      : this.statusFilter
        ? this.softwareService.listByStatus(this.statusFilter, this.page)
        : this.softwareService.list(this.page);
    obs.subscribe({
      next: (res) => {
        this.licenses = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load licenses';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  save(): void {
    const op = this.editId
      ? this.softwareService.update(this.editId, this.form)
      : this.softwareService.create(this.form);
    op.subscribe({
      next: () => {
        this.showForm = false;
        this.success = 'Saved';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed'; this.cdr.markForCheck(); },
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.softwareService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Deleted';
        this.cdr.markForCheck();
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete';
        this.cdr.markForCheck();
      },
    });
  }

  statusClass(s: string): string {
    return (
      {
        ACTIVE: 'text-bg-success',
        EXPIRED: 'text-bg-danger',
        EXPIRING_SOON: 'text-bg-warning',
        CANCELLED: 'text-bg-secondary',
      }[s] || 'text-bg-light'
    );
  }
  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
