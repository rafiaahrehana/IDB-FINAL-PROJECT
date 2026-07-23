import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { PortalService } from '../../../portal/portal.service';
import { GatewayPaymentService } from '../../../../core/services/gateway-payment.service';
import { SubscriptionPlanService, SubscriptionPlanOption } from '../../services/subscription-plan.service';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-subscription-plan',
  imports: [CommonModule, Loader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './subscription-plan.html',
  styleUrls: ['./subscription-plan.scss'],
})
export class SubscriptionPlan implements OnInit {
  trustBadges = ['No setup fees', 'Cancel anytime', 'Secure & reliable', '24/7 Support'];

  loading = false;
  error = '';
  upgradingPlanId: number | null = null;

  currentPlanCode = '';
  currentPlanPrice = 0;
  plans: SubscriptionPlanOption[] = [];

  constructor(
    private portalService: PortalService,
    private planService: SubscriptionPlanService,
    private gatewayPayment: GatewayPaymentService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    forkJoin({
      company: this.portalService.getMyCompany(),
      plans: this.planService.list(),
    }).subscribe({
      next: ({ company, plans }) => {
        this.currentPlanCode = company.subscriptionPlan || '';
        this.plans = plans;
        this.currentPlanPrice = plans.find(p => p.code === this.currentPlanCode)?.price ?? 0;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load subscription plans';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  isCurrent(plan: SubscriptionPlanOption): boolean {
    return plan.code === this.currentPlanCode;
  }

  // A plan the company isn't already on and that costs more - matches the
  // backend's own upgrade-eligibility rule (SslCommerzServiceImpl.validateTarget).
  isUpgrade(plan: SubscriptionPlanOption): boolean {
    return !this.isCurrent(plan) && plan.price > this.currentPlanPrice;
  }

  upgrade(plan: SubscriptionPlanOption): void {
    this.upgradingPlanId = plan.id;
    this.error = '';
    this.cdr.markForCheck();
    this.gatewayPayment.redirectToGateway(
      'PLATFORM_SUBSCRIPTION',
      plan.id,
      plan.price,
      (msg) => {
        this.error = msg;
        this.upgradingPlanId = null;
        this.cdr.markForCheck();
      },
    );
  }
}
