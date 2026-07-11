import { GatewayPaymentService } from '../../../../core/services/gateway-payment.service';
import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
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
  paymentAmount: number | null = null;
  paymentMethod = 'BANK_TRANSFER';
  statuses = ['DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED'];

  constructor(
    private invoiceService: InvoiceService,
    private gatewayPayment: GatewayPaymentService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    const obs = this.statusFilter
      ? this.invoiceService.listByStatus(this.statusFilter, this.page)
      : this.invoiceService.list(this.page);
    obs.subscribe({
      next: (res) => {
        this.invoices = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load invoices';
        this.loading = false;
        this.cdr.markForCheck();
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
    this.invoiceService.markPaid(invoice.id).subscribe({
      next: () => {
        this.success = 'Marked as paid';
        this.load();
        if (this.selected?.id === invoice.id) this.view(invoice);
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
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

  // SSLCommerz checkout for the remaining balance; on validated success the
  // backend records the payment (recordPaymentForCompany)
  payOnline(): void {
    if (!this.selected) return;
    const remaining = (this.selected.totalAmount ?? 0) - (this.selected.paidAmount ?? 0);
    if (remaining <= 0) {
      this.error = 'This invoice has no outstanding balance';
      return;
    }
    this.gatewayPayment.redirectToGateway('INVOICE', this.selected.id, remaining,
      (msg) => (this.error = msg));
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
