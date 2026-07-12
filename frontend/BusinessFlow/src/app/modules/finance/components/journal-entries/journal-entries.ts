import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartOfAccount, JournalEntry } from '../../models/finance.model';
import { JournalEntryService } from '../../services/journal-entry.service';
import { CoaService } from '../../services/coa.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-journal-entries',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './journal-entries.html',
})
export class JournalEntries implements OnInit {
  // VARIABLES
  entries: JournalEntry[] = [];
  accounts: ChartOfAccount[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  showForm = false;
  form: Partial<JournalEntry> = {};
  approveTarget: JournalEntry | null = null;
  postTarget: JournalEntry | null = null;
  deleteTarget: JournalEntry | null = null;

  constructor(
    private journalEntryService: JournalEntryService,
    private coaService: CoaService,
    private cdr: ChangeDetectorRef,
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void {
    this.load();
  }

  // LOAD JOURNAL ENTRIES
  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.journalEntryService.list(this.page).subscribe({
      next: (res) => {
        this.entries = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load journal entries';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  // LOAD ACCOUNTS FOR DROPDOWNS
  loadAccounts(): void {
    if (this.accounts.length) return;
    this.coaService.list(0, 200).subscribe({
      next: (res) => { this.accounts = res.content; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load accounts'; this.cdr.markForCheck(); },
    });
  }

  // OPEN CREATE FORM
  openCreate(): void {
    this.form = {};
    this.showForm = true;
    this.loadAccounts();
  }

  // SAVE JOURNAL ENTRY
  save(): void {
    this.journalEntryService.create(this.form).subscribe({
      next: () => {
        this.showForm = false;
        this.form = {};
        this.success = 'Journal entry created';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed to create journal entry'; this.cdr.markForCheck(); },
    });
  }

  // APPROVE ENTRY
  doApprove(): void {
    if (!this.approveTarget) return;
    this.journalEntryService.approve(this.approveTarget.id).subscribe({
      next: () => {
        this.approveTarget = null;
        this.success = 'Journal entry approved';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to approve';
        this.approveTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  // POST ENTRY TO LEDGER
  doPost(): void {
    if (!this.postTarget) return;
    this.journalEntryService.post(this.postTarget.id).subscribe({
      next: () => {
        this.postTarget = null;
        this.success = 'Journal entry posted';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to post';
        this.postTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  // DELETE ENTRY
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.journalEntryService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Journal entry deleted';
        this.cdr.markForCheck();
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete journal entry';
        this.cdr.markForCheck();
      },
    });
  }

  // PAGINATION
  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  // STATUS HELPERS
  statusLabel(e: JournalEntry): string {
    return e.posted ? 'POSTED' : e.approved ? 'APPROVED' : 'DRAFT';
  }
  statusClass(e: JournalEntry): string {
    return e.posted ? 'text-bg-primary' : e.approved ? 'text-bg-success' : 'text-bg-secondary';
  }
}
