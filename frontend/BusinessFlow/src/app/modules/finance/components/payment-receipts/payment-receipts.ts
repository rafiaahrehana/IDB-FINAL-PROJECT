import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PAYMENT_METHODS, PaymentReceipt, PaymentReceiptRequest } from '../../models/finance.model';
import { PaymentReceiptService } from '../../services/payment-receipt.service';
import { Client } from '../../../crm/models/crm.model';
import { ClientService } from '../../../crm/services/client.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-payment-receipts',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './payment-receipts.html',
})
export class PaymentReceipts implements OnInit {
  receipts: PaymentReceipt[] = [];
  clients: Client[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  form: PaymentReceiptRequest = this.emptyForm();
  methods = PAYMENT_METHODS;

  depositTarget: PaymentReceipt | null = null;
  depositBank = '';
  deleteTarget: PaymentReceipt | null = null;

  constructor(private receiptService: PaymentReceiptService, private clientService: ClientService) {}

  ngOnInit(): void {
    this.load();
  }

  emptyForm(): PaymentReceiptRequest {
    return { clientId: 0, amount: 0, paymentDate: new Date().toISOString().slice(0, 10), paymentMethod: 'BANK_TRANSFER', transactionReference: '', notes: '' };
  }

  load(): void {
    this.loading = true;
    this.receiptService.list(this.page).subscribe({
      next: (res) => {
        this.receipts = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load payment receipts';
        this.loading = false;
      },
    });
  }

  loadClients(): void {
    if (this.clients.length) return;
    this.clientService.list(0, 200).subscribe({ next: (res) => (this.clients = res.content), error: () => (this.clients = []) });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.showForm = true;
    this.loadClients();
  }

  save(): void {
    if (!this.form.clientId || !this.form.amount) return;
    this.receiptService.create(this.form).subscribe({
      next: () => {
        this.showForm = false;
        this.success = 'Payment receipt created';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to create payment receipt'),
    });
  }

  confirm(r: PaymentReceipt): void {
    this.receiptService.confirm(r.id).subscribe({
      next: () => {
        this.success = 'Payment confirmed';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to confirm'),
    });
  }

  openDeposit(r: PaymentReceipt): void {
    this.depositTarget = r;
    this.depositBank = '';
  }

  doDeposit(): void {
    if (!this.depositTarget || !this.depositBank.trim()) return;
    this.receiptService.markAsDeposited(this.depositTarget.id, this.depositBank.trim()).subscribe({
      next: () => {
        this.depositTarget = null;
        this.success = 'Marked as deposited';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to mark as deposited';
        this.depositTarget = null;
      },
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.receiptService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete';
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  statusClass(status: string): string {
    return (
      { PENDING: 'text-bg-warning', CONFIRMED: 'text-bg-primary', DEPOSITED: 'text-bg-success', FAILED: 'text-bg-danger', REVERSED: 'text-bg-secondary' }[status] ||
      'text-bg-secondary'
    );
  }
}
