import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import {
  DashboardService,
  DashboardSummary,
  PlatformSummary,
  ClientSummary,
} from '../../../core/services/dashboard.service';
import { AiService } from '../../../core/services/ai.service';
import { Loader } from '../../../shared/components/loader/loader';
import { StatCard } from '../../../shared/components/stat-card/stat-card';

interface QuickLink {
  label: string;
  description: string;
  icon: string;
  link: string;
}

// The dashboard is role-aware. Backend endpoints and their @PreAuthorize roles:
//   /api/dashboard/summary + /recommendations + /insights -> COMPANY_OWNER, EMPLOYEE
//   /api/dashboard/platform-summary -> all 7 platform staff roles
//   /api/dashboard/client-summary   -> CLIENT
const COMPANY_ROLES = ['COMPANY_OWNER', 'EMPLOYEE'];
// Roles allowed into /platform routes (must match RoleGuard data in app.routes.ts)
const PLATFORM_ADMIN_ROLES = ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'];

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, Loader, StatCard],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  summary?: DashboardSummary;
  platformSummary?: PlatformSummary;
  clientSummary?: ClientSummary;
  recommendations: any[] = [];
  insights = '';
  insightsError = '';
  insightsLoading = false;
  loading = false;

  // Role flags resolved once from the logged-in user
  isCompanyRole = false;
  isPlatformRole = false;
  isClientRole = false;
  canManagePlatform = false;

  subtitle = '';
  quickLinks: QuickLink[] = [];

  constructor(
    public auth: AuthService,
    private dashboardService: DashboardService,
    private aiService: AiService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const roles = this.auth.getCurrentUser()?.roles ?? [];

    this.isCompanyRole = roles.some(r => COMPANY_ROLES.includes(r));
    this.isClientRole = roles.includes('CLIENT');
    this.isPlatformRole = !this.isCompanyRole && !this.isClientRole && roles.length > 0;
    this.canManagePlatform = roles.some(r => PLATFORM_ADMIN_ROLES.includes(r));

    if (this.isCompanyRole) {
      this.subtitle = 'Live overview of your company';
      this.loadCompanyDashboard();
    } else if (this.isPlatformRole) {
      this.subtitle = 'Platform overview';
      this.quickLinks = this.buildPlatformQuickLinks(roles);
      this.loadPlatformDashboard();
    } else if (this.isClientRole) {
      this.subtitle = 'Welcome back';
      this.quickLinks = this.buildClientQuickLinks();
      this.loadClientDashboard();
    }
  }

  // ---------- COMPANY_OWNER / EMPLOYEE ----------

  private loadCompanyDashboard(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.dashboardService.getSummary().subscribe({
      next: (s) => {
        this.summary = s;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
    this.aiService.recommendations().subscribe({
      next: (recs) => {
        this.recommendations = recs;
        this.cdr.markForCheck();
      },
    });
  }

  generateInsights(): void {
    this.insightsLoading = true;
    this.insightsError = '';
    this.cdr.markForCheck();
    this.aiService.insights().subscribe({
      next: (res) => {
        this.insights = res.insights;
        this.insightsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.insightsError =
          err?.error?.message || 'Insights failed - check your AI provider config';
        this.insightsLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  // ---------- PLATFORM STAFF ----------

  private loadPlatformDashboard(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.dashboardService.getPlatformSummary().subscribe({
      next: (s) => {
        this.platformSummary = s;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  private buildPlatformQuickLinks(roles: string[]): QuickLink[] {
    const links: QuickLink[] = [];
    if (this.canManagePlatform) {
      links.push(
        { label: 'Companies', description: 'Manage tenant companies', icon: 'bi-buildings', link: '/platform/companies' },
        { label: 'Platform Users', description: 'SaaS staff accounts', icon: 'bi-person-badge', link: '/platform/platform-users' },
        { label: 'Feature Flags', description: 'Toggle platform features', icon: 'bi-toggles', link: '/platform/feature-flags' },
        { label: 'Custom Roles', description: 'Role permission sets', icon: 'bi-shield-check', link: '/platform/custom-roles' },
      );
    }
    if (roles.includes('SUPPORT_AGENT') || roles.includes('SUPPORT_MANAGER')) {
      links.push(
        { label: 'Support Tickets', description: 'Resolve tenant tickets', icon: 'bi-ticket-perforated', link: '/support/tickets' },
        { label: 'SLA Policies', description: 'Response time targets', icon: 'bi-stopwatch', link: '/support/sla-policies' },
      );
    }
    links.push(
      { label: 'Notifications', description: 'Your latest updates', icon: 'bi-bell', link: '/notifications' },
      { label: 'Settings', description: 'Profile & preferences', icon: 'bi-gear', link: '/notifications/preferences' },
    );
    return links;
  }

  // ---------- CLIENT ----------

  private loadClientDashboard(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.dashboardService.getClientSummary().subscribe({
      next: (s) => {
        this.clientSummary = s;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  private buildClientQuickLinks(): QuickLink[] {
    return [
      { label: 'My Service Requests', description: 'Track your requests', icon: 'bi-clipboard-check', link: '/servicedesk/requests' },
      { label: 'Knowledge Base', description: 'Guides and answers', icon: 'bi-journal-text', link: '/servicedesk/kb' },
      { label: 'Notifications', description: 'Your latest updates', icon: 'bi-bell', link: '/notifications' },
      { label: 'Settings', description: 'Profile & preferences', icon: 'bi-gear', link: '/notifications/preferences' },
    ];
  }

  // ---------- shared ----------

  fmt(n: number | undefined): string {
    if (n == null) return '-';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(n);
  }
}
