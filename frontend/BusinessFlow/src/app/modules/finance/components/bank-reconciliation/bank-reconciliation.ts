import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BankReconciliation, BankReconciliationRequest, ChartOfAccount } from '../../models/finance.model';
import { BankReconciliationService } from '../../services/bank-reconciliation.service';
import { CoaService } from '../../services/coa.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-bank-reconciliation',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, HasPermissionDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './bank-reconciliation.html',
})
export class BankReconciliationPage implements OnInit {
  items: BankReconciliation[] = [];
  pending: BankReconciliation[] = [];
  accounts: ChartOfAccount[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  form: BankReconciliationRequest = this.emptyForm();

  reconcileTarget: BankReconciliation | null = null;
  reconcileNotes = '';

  constructor(private reconService: BankReconciliationService, private coaService: CoaService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.coaService.list(0, 200).subscribe({ next: (res) => { this.accounts = res.content; this.cdr.markForCheck(); } });
    this.load();
    this.loadPending();
  }

  emptyForm(): BankReconciliationRequest {
    return { bankAccountId: 0, bankStatementBalance: 0 };
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.reconService.list(this.page).subscribe({
      next: (res) => {
        this.items = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load reconciliations';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadPending(): void {
    this.reconService.getPending().subscribe({ next: (res) => { this.pending = res; this.cdr.markForCheck(); } });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.showForm = true;
  }

  save(): void {
    if (!this.form.bankAccountId || !this.form.bankStatementBalance) return;
    this.reconService.create(this.form).subscribe({
      next: () => {
        this.showForm = false;
        this.success = 'Reconciliation created';
        this.cdr.markForCheck();
        this.load();
        this.loadPending();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed to create reconciliation'; this.cdr.markForCheck(); },
    });
  }

  openReconcile(item: BankReconciliation): void {
    this.reconcileTarget = item;
    this.reconcileNotes = '';
  }

  doReconcile(): void {
    if (!this.reconcileTarget) return;
    this.reconService.markAsReconciled(this.reconcileTarget.id, this.reconcileNotes || undefined).subscribe({
      next: () => {
        this.reconcileTarget = null;
        this.success = 'Marked as reconciled';
        this.cdr.markForCheck();
        this.load();
        this.loadPending();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to reconcile';
        this.reconcileTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
