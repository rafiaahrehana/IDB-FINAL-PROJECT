import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Company,
  COMPANY_STATUSES,
  CompanyStatus,
  RegisterCompanyRequest,
  SUBSCRIPTION_PLANS,
  SubscriptionPlan,
} from '../../models/platform-admin.model';
import { CompanyService } from '../../services/company.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-companies',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './companies.html',
})
export class Companies implements OnInit {
  // VARIABLES
  companies: Company[] = [];
  totalPages = 0;
  totalCompanies = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  statusFilter: CompanyStatus | '' = '';

  // KPI COUNTS DERIVED FROM CURRENT LOAD
  kpi = { total: 0, active: 0, trial: 0, suspended: 0 };
  loadingKpi = false;

  // REGISTER MODAL
  showRegister = false;
  form: RegisterCompanyRequest = {
    companyName: '', subdomain: '', ownerFirstName: '', ownerLastName: '', ownerEmail: '', ownerPassword: ''
  };

  // PLAN CHANGE
  planTarget: Company | null = null;
  newPlan: SubscriptionPlan = 'STARTER';

  // STATUS CHANGE
  statusTarget: Company | null = null;
  newStatus: CompanyStatus = 'ACTIVE';

  deactivateTarget: Company | null = null;

  statuses = COMPANY_STATUSES;
  plans = SUBSCRIPTION_PLANS;

  constructor(private companyService: CompanyService) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void {
    this.load();
    this.loadKpi();
  }

  // LOAD COMPANIES (RESPECTS STATUS FILTER)
  load(): void {
    this.loading = true;
    this.error = '';
    this.companyService.list(this.page, 20, this.statusFilter || undefined).subscribe({
      next: (res) => {
        this.companies = res.content;
        this.totalPages = res.totalPages;
        this.totalCompanies = res.totalElements;
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load companies'; this.loading = false; }
    });
  }

  // LOAD KPI COUNTS BY FETCHING COUNTS PER STATUS (SEPARATE FROM MAIN LIST)
  // Uses a single unfiltered page-1 call for total, and per-status calls for the breakdown
  loadKpi(): void {
    this.loadingKpi = true;
    this.companyService.list(0, 1).subscribe({
      next: (res) => { this.kpi.total = res.totalElements; this.tryFinishKpi(); },
      error: () => this.loadingKpi = false
    });
    this.companyService.list(0, 1, 'ACTIVE').subscribe({
      next: (res) => { this.kpi.active = res.totalElements; this.tryFinishKpi(); },
      error: () => { /* no-op */ }
    });
    this.companyService.list(0, 1, 'TRIAL').subscribe({
      next: (res) => { this.kpi.trial = res.totalElements; this.tryFinishKpi(); },
      error: () => { /* no-op */ }
    });
    this.companyService.list(0, 1, 'SUSPENDED').subscribe({
      next: (res) => { this.kpi.suspended = res.totalElements; this.tryFinishKpi(); },
      error: () => { /* no-op */ }
    });
  }

  private kpiReady = 0;
  private tryFinishKpi(): void {
    this.kpiReady++;
    if (this.kpiReady >= 4) this.loadingKpi = false;
  }

  // OPEN REGISTER MODAL
  openRegister(): void {
    this.form = { companyName: '', subdomain: '', ownerFirstName: '', ownerLastName: '', ownerEmail: '', ownerPassword: '' };
    this.showRegister = true;
  }

  // REGISTER COMPANY
  register(): void {
    this.companyService.register(this.form).subscribe({
      next: () => { this.showRegister = false; this.success = 'Company registered'; this.load(); this.kpiReady = 0; this.loadKpi(); },
      error: (err) => this.error = err?.error?.message || 'Failed to register company'
    });
  }

  // OPEN PLAN CHANGE
  openPlanChange(c: Company): void {
    this.planTarget = c;
    this.newPlan = c.subscriptionPlan;
  }

  doChangePlan(): void {
    if (!this.planTarget) return;
    this.companyService.changePlan(this.planTarget.id, this.newPlan).subscribe({
      next: () => { this.planTarget = null; this.success = 'Plan updated'; this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to change plan'; this.planTarget = null; }
    });
  }

  // OPEN STATUS CHANGE
  openStatusChange(c: Company): void {
    this.statusTarget = c;
    this.newStatus = c.status;
  }

  doChangeStatus(): void {
    if (!this.statusTarget) return;
    this.companyService.changeStatus(this.statusTarget.id, this.newStatus).subscribe({
      next: () => { this.statusTarget = null; this.success = 'Status updated'; this.load(); this.kpiReady = 0; this.loadKpi(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to change status'; this.statusTarget = null; }
    });
  }

  // DEACTIVATE
  doDeactivate(): void {
    if (!this.deactivateTarget) return;
    this.companyService.deactivate(this.deactivateTarget.id).subscribe({
      next: () => { this.deactivateTarget = null; this.success = 'Company deactivated'; this.load(); this.kpiReady = 0; this.loadKpi(); },
      error: () => { this.deactivateTarget = null; this.error = 'Cannot deactivate company'; }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // BADGE STYLING
  statusClass(status: CompanyStatus): string {
    return {
      PENDING_VERIFICATION: 'text-bg-secondary',
      TRIAL: 'text-bg-warning',
      ACTIVE: 'text-bg-success',
      SUSPENDED: 'text-bg-danger',
      DEACTIVATED: 'text-bg-secondary',
    }[status];
  }

  planClass(plan: SubscriptionPlan): string {
    return {
      FREE: 'text-bg-light border',
      STARTER: 'text-bg-info',
      PRO: 'text-bg-primary',
      ENTERPRISE: 'text-bg-dark',
    }[plan];
  }
}
