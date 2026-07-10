import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubscriptionManagementService, PlanConfig, CompanySubscription } from '../../services/subscription-management.service';

@Component({
  selector: 'app-subscription-management',
  imports: [CommonModule, FormsModule],
  templateUrl: './subscription-management.html',
})
export class SubscriptionManagement implements OnInit {
  plans: PlanConfig[] = [];
  subscriptions: CompanySubscription[] = [];
  loading = false;
  error = '';
  success = '';
  activeTab: 'plans' | 'subscriptions' = 'plans';

  editMode = false;
  editForm: Partial<PlanConfig> = {};

  plansLoading = false;
  subsLoading = false;

  constructor(private service: SubscriptionManagementService) {}

  ngOnInit(): void {
    this.loadPlans();
    this.loadSubscriptions();
  }

  switchTab(tab: 'plans' | 'subscriptions'): void {
    this.activeTab = tab;
  }

  loadPlans(): void {
    this.plansLoading = true;
    this.service.getPlans().subscribe({
      next: (plans) => { this.plans = plans; this.plansLoading = false; },
      error: () => { this.error = 'Failed to load plans'; this.plansLoading = false; },
    });
  }

  loadSubscriptions(): void {
    this.subsLoading = true;
    this.service.getCompanySubscriptions().subscribe({
      next: (subs) => { this.subscriptions = subs; this.subsLoading = false; },
      error: () => { this.error = 'Failed to load subscriptions'; this.subsLoading = false; },
    });
  }

  startCreate(): void {
    this.editMode = true;
    this.editForm = {
      plan: 'FREE' as any,
      displayName: '',
      description: '',
      price: 0,
      currency: 'BDT',
      durationDays: 30,
      featured: false,
      active: true,
      maxEmployees: 0,
      maxClients: 0,
      maxProjects: 0,
      maxStorageMb: 0,
      aiEnabled: false,
      websiteBuilderEnabled: false,
      customDomainEnabled: false,
      prioritySupport: false,
      apiAccess: false,
      features: '',
    };
  }

  startEdit(plan: PlanConfig): void {
    this.editMode = true;
    this.editForm = { ...plan };
  }

  cancelEdit(): void {
    this.editMode = false;
    this.editForm = {};
  }

  savePlan(): void {
    this.error = '';
    this.success = '';
    if (this.editForm.id) {
      this.service.updatePlan(this.editForm.id, this.editForm).subscribe({
        next: () => {
          this.success = 'Plan updated successfully';
          this.editMode = false;
          this.loadPlans();
        },
        error: (err) => { this.error = err?.error?.message || 'Failed to update plan'; },
      });
    } else {
      this.service.createPlan(this.editForm).subscribe({
        next: () => {
          this.success = 'Plan created successfully';
          this.editMode = false;
          this.loadPlans();
        },
        error: (err) => { this.error = err?.error?.message || 'Failed to create plan'; },
      });
    }
  }

  deletePlan(plan: PlanConfig): void {
    if (!confirm('Delete the ' + plan.displayName + ' plan?')) return;
    this.service.deletePlan(plan.id).subscribe({
      next: () => { this.success = 'Plan deleted'; this.loadPlans(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to delete plan'; },
    });
  }

  toggleActive(plan: PlanConfig): void {
    this.service.updatePlan(plan.id, { ...plan, active: !plan.active }).subscribe({
      next: () => { this.loadPlans(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to toggle plan'; },
    });
  }

  suspendCompany(sub: CompanySubscription): void {
    if (!confirm('Suspend company "' + sub.companyName + '"?')) return;
    this.service.suspendCompany(sub.companyId).subscribe({
      next: () => { this.success = 'Company suspended'; this.loadSubscriptions(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to suspend'; },
    });
  }

  activateCompany(sub: CompanySubscription): void {
    this.service.activateCompany(sub.companyId).subscribe({
      next: () => { this.success = 'Company activated'; this.loadSubscriptions(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to activate'; },
    });
  }

  statusBadge(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'SUSPENDED': return 'bg-danger';
      case 'EXPIRED': return 'bg-warning text-dark';
      case 'PENDING': return 'bg-info';
      case 'CANCELLED': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  }

  planBadge(plan: string): string {
    switch (plan) {
      case 'ENTERPRISE': return 'bg-dark';
      case 'PRO': return 'bg-primary';
      case 'STARTER': return 'bg-info';
      case 'FREE': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  }
}
