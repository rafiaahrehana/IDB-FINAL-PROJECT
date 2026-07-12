import { GatewayPaymentService } from '../../../../core/services/gateway-payment.service';
import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Wallet, WalletTransaction, WalletTransactionType, WALLET_TRANSACTION_TYPES } from '../../models/finance.model';
import { WalletService } from '../../services/wallet.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-wallet',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './wallet.html',
})
export class WalletPage implements OnInit {
  wallet?: Wallet;
  transactions: WalletTransaction[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  loadingWallet = false;
  error = '';
  success = '';

  typeFilter: WalletTransactionType | '' = '';
  types = WALLET_TRANSACTION_TYPES;

  showTopUp = false;
  topUpForm = { amount: 0, reference: '', notes: '' };
  saving = false;

  constructor(
    private walletService: WalletService,
    private gatewayPayment: GatewayPaymentService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadWallet();
    this.loadTransactions();
  }

  loadWallet(): void {
    this.loadingWallet = true;
    this.cdr.markForCheck();
    this.walletService.getWallet().subscribe({
      next: (w) => {
        this.wallet = w;
        this.loadingWallet = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load wallet';
        this.loadingWallet = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadTransactions(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.walletService.getTransactions(this.typeFilter || undefined, this.page).subscribe({
      next: (res) => {
        this.transactions = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load transactions';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openTopUp(): void {
    this.topUpForm = { amount: 0, reference: '', notes: '' };
    this.showTopUp = true;
  }

  doTopUp(): void {
    if (!this.topUpForm.amount || this.topUpForm.amount <= 0) return;
    this.saving = true;
    this.cdr.markForCheck();
    this.walletService.topUp(this.topUpForm).subscribe({
      next: (w) => {
        this.wallet = w;
        this.saving = false;
        this.showTopUp = false;
        this.success = 'Wallet topped up successfully';
        this.page = 0;
        this.cdr.markForCheck();
        this.loadTransactions();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to top up wallet';
        this.cdr.markForCheck();
      },
    });
  }

  // SSLCommerz checkout; on validated success the backend credits the wallet
  topUpOnline(): void {
    if (!this.topUpForm.amount || this.topUpForm.amount <= 0) return;
    this.gatewayPayment.redirectToGateway('WALLET_TOPUP', null, this.topUpForm.amount,
      (msg) => { this.error = msg; this.cdr.markForCheck(); });
  }

  goToPage(p: number): void {
    this.page = p;
    this.loadTransactions();
  }

  typeClass(type: string): string {
    return (
      {
        CREDIT: 'text-bg-success',
        REFUND_CREDIT: 'text-bg-success',
        REFERRAL_REWARD: 'text-bg-success',
        DEBIT: 'text-bg-danger',
        CREDIT_APPLIED: 'text-bg-primary',
      }[type] || 'text-bg-secondary'
    );
  }

  typeLabel(type: string): string {
    return type.replace(/_/g, ' ').toLowerCase();
  }
}
