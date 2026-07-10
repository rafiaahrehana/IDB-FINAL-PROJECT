import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Invoice } from '../../models/finance.model';
import { InvoiceService } from '../../services/invoice.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-invoices',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './invoices.html',
})
export class Invoices implements OnInit {
  invoices: Invoice[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  selected?: Invoice;
  editing?: Invoice;
  editPayload: any = {};
  paymentAmount: number | null = null;
  paymentMethod = 'BANK_TRANSFER';
  statuses = ['DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED'];

  constructor(private invoiceService: InvoiceService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    const obs = this.statusFilter
      ? this.invoiceService.listByStatus(this.statusFilter, this.page)
      : this.invoiceService.list(this.page);
    obs.subscribe({
      next: (res) => {
        this.invoices = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load invoices';
        this.loading = false;
      },
    });
  }

  view(invoice: Invoice): void {
    this.invoiceService.getById(invoice.id).subscribe({ next: (i) => (this.selected = i) });
  }

  send(invoice: Invoice): void {
    this.invoiceService.send(invoice.id).subscribe({
      next: () => {
        this.success = 'Invoice sent';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  markPaid(invoice: Invoice): void {
    this.invoiceService.markPaid(invoice.id, invoice.balanceAmount ?? 0).subscribe({
      next: () => {
        this.success = 'Marked as paid';
        this.load();
        if (this.selected?.id === invoice.id) this.view(invoice);
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  remove(invoice: Invoice): void {
    if (!window.confirm('Delete invoice ' + invoice.invoiceNumber + '?')) return;
    this.invoiceService.delete(invoice.id).subscribe({
      next: () => {
        this.success = 'Invoice deleted';
        this.load();
        if (this.selected?.id === invoice.id) this.selected = undefined;
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to delete invoice'),
    });
  }

  openEdit(invoice: Invoice): void {
    this.editing = invoice;
    this.editPayload = {
      invoiceDate: invoice.invoiceDate,
      dueDate: invoice.dueDate,
      notes: invoice.notes,
    };
  }

  closeEdit(): void {
    this.editing = undefined;
    this.editPayload = {};
  }

  saveEdit(): void {
    if (!this.editing) return;
    this.invoiceService.update(this.editing.id, this.editPayload).subscribe({
      next: () => {
        this.success = 'Invoice updated';
        this.closeEdit();
        this.load();
        if (this.selected?.id === this.editing?.id) this.view(this.selected!);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update invoice';
        this.closeEdit();
      },
    });
  }

  recordPayment(): void {
    if (!this.selected || !this.paymentAmount) return;
    this.invoiceService
      .recordPayment(this.selected.id, this.paymentAmount, this.paymentMethod)
      .subscribe({
        next: () => {
          this.success = 'Payment recorded';
          this.paymentAmount = null;
          this.view(this.selected!);
          this.load();
        },
        error: (err) => (this.error = err?.error?.message || 'Failed'),
      });
  }

  statusClass(s: string): string {
    return (
      {
        PAID: 'text-bg-success',
        ISSUED: 'text-bg-primary',
        OVERDUE: 'text-bg-danger',
        CANCELLED: 'text-bg-secondary',
        DRAFT: 'text-bg-light',
        PARTIALLY_PAID: 'text-bg-warning',
      }[s] || 'text-bg-secondary'
    );
  }
  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
