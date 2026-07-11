import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  BILLING_CYCLES,
  CompanyService,
  PackageSubscription,
  ServicePackage,
  ServicePackageRequest,
  SUBSCRIPTION_STATUSES,
  SubscriptionStatus,
} from '../../models/servicedesk.model';
import { GatewayPaymentService } from '../../../../core/services/gateway-payment.service';
import { ServicePackageService } from '../../services/service-package.service';
import { CompanyServiceService } from '../../services/company-service.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

type Tab = 'packages' | 'subscriptions';

@Component({
  selector: 'app-service-packages',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './packages.html',
})
export class Packages implements OnInit {
  // VARIABLES
  tab: Tab = 'packages';

  packages: ServicePackage[] = [];
  subscriptions: PackageSubscription[] = [];
  availableServices: CompanyService[] = [];

  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: ServicePackageRequest = { name: '', packagePrice: 0 };

  deleteTarget: ServicePackage | null = null;
  cancelTarget: PackageSubscription | null = null;
  cancelReason = '';

  billingCycles = BILLING_CYCLES;
  subscriptionStatuses = SUBSCRIPTION_STATUSES;

  constructor(
    private packageService: ServicePackageService,
    private gatewayPayment: GatewayPaymentService,
    private serviceService: CompanyServiceService,
    private cdr: ChangeDetectorRef,
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // SWITCH TAB
  setTab(tab: Tab): void {
    if (this.tab === tab) return;
    this.tab = tab;
    this.page = 0;
    this.load();
  }

  // LOAD BASED ON TAB
  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    if (this.tab === 'packages') {
      this.packageService.list(this.page).subscribe({
        next: (res) => { this.packages = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to load'; this.loading = false; this.cdr.markForCheck(); }
      });
    } else {
      this.packageService.listSubscriptions(this.page).subscribe({
        next: (res) => { this.subscriptions = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
        error: () => { this.error = 'Failed to load'; this.loading = false; this.cdr.markForCheck(); }
      });
    }
  }

  // LAZY LOAD ACTIVE SERVICES FOR PACKAGE COMPOSITION
  loadServices(): void {
    if (this.availableServices.length) return;
    this.serviceService.listActive().subscribe({
      next: (res) => this.availableServices = res,
      error: () => this.availableServices = []
    });
  }

  // OPEN CREATE / EDIT PACKAGE FORM
  openCreate(): void {
    this.editingId = null;
    this.form = { name: '', packagePrice: 0, serviceIds: [] };
    this.showForm = true;
    this.loadServices();
  }

  openEdit(p: ServicePackage): void {
    this.editingId = p.id;
    this.form = {
      name: p.name,
      nameBn: p.nameBn,
      description: p.description,
      descriptionBn: p.descriptionBn,
      iconUrl: p.iconUrl,
      packagePrice: p.packagePrice,
      discountPercent: p.discountPercent,
      billingCycle: p.billingCycle,
      requestQuota: p.requestQuota,
      autoRenew: p.autoRenew,
      deliveryDays: p.deliveryDays,
      terms: p.terms,
      featured: p.featured,
      popular: p.popular,
      serviceIds: (p.services || []).map(s => s.id),
    };
    this.showForm = true;
    this.loadServices();
  }

  // SAVE PACKAGE
  save(): void {
    const op = this.editingId
      ? this.packageService.update(this.editingId, this.form)
      : this.packageService.create(this.form);
    op.subscribe({
      next: () => {
        this.success = this.editingId ? 'Package updated' : 'Package created';
        this.showForm = false; this.editingId = null; this.load();
      },
      error: (err) => this.error = err?.error?.message || 'Failed to save package'
    });
  }

  // TOGGLE INCLUSION OF A SERVICE IN THE PACKAGE
  toggleService(serviceId: number): void {
    const ids = this.form.serviceIds || [];
    const i = ids.indexOf(serviceId);
    if (i > -1) ids.splice(i, 1); else ids.push(serviceId);
    this.form.serviceIds = ids;
  }

  isServiceSelected(serviceId: number): boolean {
    return (this.form.serviceIds || []).indexOf(serviceId) > -1;
  }

  // TOGGLE ACTIVE
  toggle(p: ServicePackage): void {
    this.packageService.toggle(p.id).subscribe({
      next: () => this.load(),
      error: (err) => this.error = err?.error?.message || 'Failed to toggle package'
    });
  }

  // DELETE PACKAGE
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.packageService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Package deleted'; this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete package'; }
    });
  }

  // SUBSCRIPTION ACTIONS
  activateSubscription(s: PackageSubscription): void {
    this.packageService.activateSubscription(s.id).subscribe({
      next: () => { this.success = 'Subscription activated'; this.load(); },
      error: (err) => this.error = err?.error?.message || 'Failed to activate'
    });
  }

  // SSLCommerz checkout for a pending subscription; on validated success the
  // backend activates it (activateForCompany) and redirects to /payment-result
  payOnline(s: PackageSubscription): void {
    if (!s.pricePaid || s.pricePaid <= 0) {
      this.error = 'This subscription has no payable amount - use Activate instead';
      return;
    }
    this.gatewayPayment.redirectToGateway('PACKAGE_SUBSCRIPTION', s.id, s.pricePaid,
      (msg) => (this.error = msg));
  }

  suspendSubscription(s: PackageSubscription): void {
    this.packageService.suspendSubscription(s.id).subscribe({
      next: () => { this.success = 'Subscription suspended'; this.load(); },
      error: (err) => this.error = err?.error?.message || 'Failed to suspend'
    });
  }

  reactivateSubscription(s: PackageSubscription): void {
    this.packageService.reactivateSubscription(s.id).subscribe({
      next: () => { this.success = 'Subscription reactivated'; this.load(); },
      error: (err) => this.error = err?.error?.message || 'Failed to reactivate'
    });
  }

  openCancel(s: PackageSubscription): void {
    this.cancelTarget = s;
    this.cancelReason = '';
  }

  doCancel(): void {
    if (!this.cancelTarget) return;
    this.packageService.cancelSubscription(this.cancelTarget.id, this.cancelReason).subscribe({
      next: () => { this.cancelTarget = null; this.success = 'Subscription cancelled'; this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to cancel'; this.cancelTarget = null; }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // BADGES
  subscriptionStatusClass(status: SubscriptionStatus): string {
    return {
      ACTIVE: 'text-bg-success',
      PENDING_PAYMENT: 'text-bg-warning',
      SUSPENDED: 'text-bg-secondary',
      EXPIRED: 'text-bg-secondary',
      CANCELLED: 'text-bg-danger',
    }[status] || 'text-bg-secondary';
  }
}
