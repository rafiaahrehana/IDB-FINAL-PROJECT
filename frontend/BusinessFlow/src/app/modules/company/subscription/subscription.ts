import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SubscriptionService, PlanInfo, SubscriptionResponse } from '../services/subscription.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-subscription',
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './subscription.html',
})
export class SubscriptionPage implements OnInit {
  plans: PlanInfo[] = [];
  current?: SubscriptionResponse;
  loading = false;
  error = '';
  success = '';
  processing = '';

  cusName = '';
  cusEmail = '';

  constructor(
    private subService: SubscriptionService,
    private auth: AuthService,
    private router: Router,
  ) {
    const user = this.auth.getCurrentUser();
    if (user) {
      this.cusName = `${user.firstName} ${user.lastName}`.trim();
      this.cusEmail = user.email || '';
    }
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.subService.getPlans().subscribe({
      next: (plans) => {
        this.plans = plans;
        this.subService.getCurrentSubscription().subscribe({
          next: (sub) => { this.current = sub; this.loading = false; },
          error: () => { this.loading = false; },
        });
      },
      error: () => { this.error = 'Failed to load plans'; this.loading = false; },
    });
  }

  subscribe(plan: PlanInfo): void {
    if (plan.price === 0) return;
    if (!this.cusName || !this.cusEmail) return;
    this.processing = plan.name;
    this.error = '';

    this.subService.checkout(plan.name, this.cusName, this.cusEmail).subscribe({
      next: (res) => {
        if (res.success && res.gatewayPageUrl) {
          window.location.href = res.gatewayPageUrl;
        } else {
          this.error = res.message || 'Failed to initiate payment';
          this.processing = '';
        }
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to connect to payment gateway';
        this.processing = '';
      },
    });
  }

  planButtonLabel(plan: PlanInfo): string {
    if (this.processing === plan.name) return 'Processing...';
    if (this.current?.plan === plan.name && this.current?.status === 'ACTIVE') return 'Current Plan';
    if (plan.price === 0) return 'Free Forever';
    if (this.current?.plan === 'FREE' || !this.current) return 'Subscribe Now';
    return 'Upgrade';
  }

  isCurrentPlan(plan: PlanInfo): boolean {
    return this.current?.plan === plan.name && this.current?.status === 'ACTIVE';
  }

  statusClass(): string {
    if (!this.current) return 'bg-secondary';
    return this.current.status === 'ACTIVE' ? 'bg-success' : this.current.status === 'EXPIRED' ? 'bg-danger' : 'bg-warning';
  }
}
