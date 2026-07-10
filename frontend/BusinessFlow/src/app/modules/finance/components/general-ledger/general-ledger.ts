import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartOfAccount, GeneralLedgerEntry } from '../../models/finance.model';
import { GeneralLedgerService } from '../../services/general-ledger.service';
import { CoaService } from '../../services/coa.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-general-ledger',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './general-ledger.html',
})
export class GeneralLedger implements OnInit {
  entries: GeneralLedgerEntry[] = [];
  accounts: ChartOfAccount[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  accountFilter?: number;
  startDate = '';
  endDate = '';
  accountBalance: number | null = null;

  reconcileTarget: GeneralLedgerEntry | null = null;
  reconcileNotes = '';

  constructor(private ledgerService: GeneralLedgerService, private coaService: CoaService) {}

  ngOnInit(): void {
    this.coaService.list(0, 200).subscribe({ next: (res) => (this.accounts = res.content) });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.accountBalance = null;
    let obs;
    if (this.startDate && this.endDate) {
      obs = this.ledgerService.getByDateRange(this.startDate, this.endDate, this.page);
    } else if (this.accountFilter) {
      obs = this.ledgerService.getByAccount(this.accountFilter, this.page);
      this.ledgerService.getAccountBalance(this.accountFilter).subscribe({ next: (b) => (this.accountBalance = b) });
    } else {
      obs = this.ledgerService.list(this.page);
    }
    obs.subscribe({
      next: (res) => {
        this.entries = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load ledger entries';
        this.loading = false;
      },
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.load();
  }

  clearFilters(): void {
    this.accountFilter = undefined;
    this.startDate = '';
    this.endDate = '';
    this.page = 0;
    this.load();
  }

  openReconcile(e: GeneralLedgerEntry): void {
    this.reconcileTarget = e;
    this.reconcileNotes = '';
  }

  doReconcile(): void {
    if (!this.reconcileTarget) return;
    this.ledgerService.reconcile(this.reconcileTarget.id, this.reconcileNotes || undefined).subscribe({
      next: () => {
        this.reconcileTarget = null;
        this.success = 'Entry reconciled';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to reconcile';
        this.reconcileTarget = null;
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
