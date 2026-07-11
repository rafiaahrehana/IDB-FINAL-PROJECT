import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartOfAccount } from '../../models/finance.model';
import { CoaService } from '../../services/coa.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-chart-of-accounts',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './chart-of-accounts.html',
})
export class ChartOfAccounts implements OnInit {
  accounts: ChartOfAccount[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  showForm = false;
  editId: number | null = null;
  deleteTarget: ChartOfAccount | null = null;
  form: Partial<ChartOfAccount> = {};
  accountTypes = ['ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'];

  constructor(private coaService: CoaService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.coaService.list(this.page).subscribe({
      next: (res) => {
        this.accounts = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load accounts';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    this.editId = null;
    this.form = { active: true, allowDirectPosting: true };
    this.showForm = true;
    this.error = '';
  }

  openEdit(a: ChartOfAccount): void {
    this.editId = a.id;
    this.form = {
      accountCode: a.accountCode,
      accountName: a.accountName,
      type: a.type,
      description: a.description,
      active: a.active,
    };
    this.showForm = true;
    this.error = '';
  }

  save(): void {
    const op = this.editId
      ? this.coaService.update(this.editId, this.form)
      : this.coaService.create(this.form);
    op.subscribe({
      next: () => {
        this.showForm = false;
        this.success = 'Saved';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to save'),
    });
  }

  confirmDelete(a: ChartOfAccount): void {
    this.deleteTarget = a;
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.coaService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Deleted';
        this.load();
      },
      error: (err) => {
        this.deleteTarget = null;
        this.error = err?.error?.message || 'Cannot delete';
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
