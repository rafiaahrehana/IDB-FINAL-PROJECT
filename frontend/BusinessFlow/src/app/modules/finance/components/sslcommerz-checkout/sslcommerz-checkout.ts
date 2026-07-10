import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SslCommerzService } from '../../services/sslcommerz.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-sslcommerz-checkout',
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './sslcommerz-checkout.html',
})
export class SslCommerzCheckout {
  amount = 0;
  cusName = '';
  cusEmail = '';
  cusPhone = '';
  processing = false;
  error = '';

  constructor(
    private sslService: SslCommerzService,
    private auth: AuthService,
    private router: Router,
  ) {
    const user = this.auth.getCurrentUser();
    if (user) {
      this.cusName = `${user.firstName} ${user.lastName}`.trim();
      this.cusEmail = user.email || '';
    }
  }

  pay(): void {
    if (!this.amount || this.amount < 10 || !this.cusName || !this.cusEmail) return;
    this.processing = true;
    this.error = '';

    this.sslService.initPayment({
      amount: this.amount,
      cusName: this.cusName,
      cusEmail: this.cusEmail,
      cusPhone: this.cusPhone,
    }).subscribe({
      next: (res) => {
        if (res.success && res.gatewayPageUrl) {
          window.location.href = res.gatewayPageUrl;
        } else {
          this.error = res.message || 'Failed to initiate payment';
          this.processing = false;
        }
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to connect to payment gateway';
        this.processing = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/finance/wallet']);
  }
}
