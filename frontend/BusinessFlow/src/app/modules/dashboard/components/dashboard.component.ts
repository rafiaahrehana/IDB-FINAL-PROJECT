import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PermissionService } from '../../../core/services/permission.service';
import {
  DashboardService,
  DashboardSummary,
  PlatformSummary,
  PlatformMetricsPoint,
  ClientSummary,
  RecommendationResponse,
} from '../../../core/services/dashboard.service';
import { Loader } from '../../../shared/components/loader/loader';
import { StatCard } from '../../../shared/components/stat-card/stat-card';
import { WIDGET_REGISTRY, DASHBOARD_SECTIONS, DashboardWidgetDef, DashboardSection } from '../widget-registry';

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
  readonly dashboardSections = DASHBOARD_SECTIONS;
  summary?: DashboardSummary;
  platformSummary?: PlatformSummary;
  platformMetricsHistory: PlatformMetricsPoint[] = [];
  clientSummary?: ClientSummary;
  recommendations: RecommendationResponse[] = [];
  insights = '';
  insightsError = '';
  insightsLoading = false;
  loading = false;
  error = '';

  // Role flags resolved once from the logged-in user
  isCompanyRole = false;
  isPlatformRole = false;
  isClientRole = false;
  canManagePlatform = false;

  subtitle = '';
  quickLinks: QuickLink[] = [];
  selectedRange: 'day' | 'week' | 'month' | 'year' = 'month';

  constructor(
    public auth: AuthService,
    private dashboardService: DashboardService,
    public permissionService: PermissionService,
    private cdr: ChangeDetectorRef,
  ) {}

  /** Widgets the current user's permission set allows, for the given dashboard section. */
  widgetsForSection(section: DashboardSection): DashboardWidgetDef[] {
    return WIDGET_REGISTRY.filter(
      w => w.section === section && this.permissionService.hasPermission(w.requiredPermission),
    );
  }

  /** Inputs passed to each widget via NgComponentOutlet. */
  widgetInputs(): Record<string, unknown> {
    return { summary: this.summary };
  }

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
      this.subtitle = "Here's what's happening with your platform today.";
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
    this.error = '';
    this.cdr.markForCheck();
    const range = this.getDateRange();
    this.dashboardService.getSummary(range?.from, range?.to).subscribe({
      next: (s) => {
        this.summary = s;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load dashboard summary';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
    this.dashboardService.getRecommendations().subscribe({
      next: (recs) => {
        this.recommendations = recs;
        this.cdr.markForCheck();
      },
      error: () => {},
    });
  }

  generateInsights(): void {
    this.insightsLoading = true;
    this.insightsError = '';
    this.cdr.markForCheck();
    this.dashboardService.getInsights().subscribe({
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
    this.error = '';
    this.cdr.markForCheck();
    this.dashboardService.getPlatformSummary().subscribe({
      next: (s) => {
        this.platformSummary = s;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load platform summary';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
    this.dashboardService.getPlatformMetricsHistory(this.platformHistoryDays()).subscribe({
      next: (h) => { this.platformMetricsHistory = h; this.cdr.markForCheck(); },
      error: () => { this.platformMetricsHistory = []; this.cdr.markForCheck(); },
    });
  }

  private platformHistoryDays(): number {
    switch (this.selectedRange) {
      case 'day': return 2;
      case 'week': return 7;
      case 'month': return 30;
      case 'year': return 365;
    }
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
      { label: 'Settings', description: 'Profile & preferences', icon: 'bi-gear', link: '/profile' },
    );
    return links;
  }

  // ---------- CLIENT ----------

  private loadClientDashboard(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    this.dashboardService.getClientSummary().subscribe({
      next: (s) => {
        this.clientSummary = s;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load client summary';
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
      { label: 'Settings', description: 'Profile & preferences', icon: 'bi-gear', link: '/profile' },
    ];
  }

  // ---------- shared ----------

  fmt(n: number | undefined): string {
    if (n == null) return '-';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'BDT',
      currencyDisplay: 'code',
      maximumFractionDigits: 0,
    }).format(n);
  }

  setRange(range: 'day' | 'week' | 'month' | 'year'): void {
    this.selectedRange = range;
    if (this.isCompanyRole) {
      this.loadCompanyDashboard();
    } else if (this.isPlatformRole) {
      this.loadPlatformDashboard();
    }
  }

  private getDateRange(): { from: string; to: string } | undefined {
    const now = new Date();
    const to = now.toISOString().split('T')[0];
    let from: string;
    switch (this.selectedRange) {
      case 'day':
        from = to;
        break;
      case 'week': {
        const d = new Date(now);
        d.setDate(d.getDate() - 7);
        from = d.toISOString().split('T')[0];
        break;
      }
      case 'month': {
        const d = new Date(now);
        d.setMonth(d.getMonth() - 1);
        from = d.toISOString().split('T')[0];
        break;
      }
      case 'year': {
        const d = new Date(now);
        d.setFullYear(d.getFullYear() - 1);
        from = d.toISOString().split('T')[0];
        break;
      }
    }
    return { from: from!, to };
  }

  get timeGreeting(): string {
    const hr = new Date().getHours();
    if (hr < 5) return 'Good Night';
    if (hr < 12) return 'Good Morning';
    if (hr < 17) return 'Good Afternoon';
    if (hr < 21) return 'Good Evening';
    return 'Good Night';
  }

  get userFirstName(): string {
    const user = this.auth.getCurrentUser();
    if (!user) return 'User';
    if (user.fullName) {
      return user.fullName.split(' ')[0];
    }
    return 'User';
  }

  get currentRangeLabel(): string {
    const range = this.getDateRange();
    if (!range) return '';
    const fromDate = new Date(range.from);
    const toDate = new Date(range.to);
    const format = (d: Date) => d.toLocaleString('en-US', { month: 'short', day: 'numeric' });
    const formatYear = (d: Date) => d.toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    return `${format(fromDate)} - ${formatYear(toDate)}`;
  }

  get rangeLabel(): string {
    switch (this.selectedRange) {
      case 'day': return 'Today';
      case 'week': return 'This Week';
      case 'month': return 'This Month';
      case 'year': return 'This Year';
    }
  }

  getYCoordinate(val: number): number {
    const maxVal = 300;
    return 180 - ((val / maxVal) * 150);
  }

  getLinePath(data: number[] | undefined): string {
    if (!data || data.length === 0) return '';
    return data.map((val, idx) => {
      const x = idx * 75 + 25;
      const y = this.getYCoordinate(val);
      return `${idx === 0 ? 'M' : 'L'} ${x} ${y}`;
    }).join(' ');
  }

  getAreaPath(data: number[] | undefined): string {
    if (!data || data.length === 0) return '';
    const linePath = this.getLinePath(data);
    const startX = 25;
    const endX = (data.length - 1) * 75 + 25;
    return `${linePath} L ${endX} 180 L ${startX} 180 Z`;
  }

  // ---------- PLATFORM: KPI sparklines (auto-scaled to each series' own min/max,
  // unlike getLinePath/getAreaPath above which assume a fixed 0-300 range sized
  // for the company Sales Overview chart) ----------

  private sparklineCoords(data: number[], width: number, height: number, padding: number): { x: number; y: number }[] {
    if (!data || data.length === 0) return [];
    const max = Math.max(...data, 1);
    const min = Math.min(...data, 0);
    const range = max - min || 1;
    const stepX = data.length > 1 ? (width - padding * 2) / (data.length - 1) : 0;
    return data.map((v, i) => ({
      x: padding + i * stepX,
      y: height - padding - ((v - min) / range) * (height - padding * 2),
    }));
  }

  sparklinePath(data: number[], width = 100, height = 32, padding = 3): string {
    const pts = this.sparklineCoords(data, width, height, padding);
    if (!pts.length) return '';
    return pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`).join(' ');
  }

  sparklineArea(data: number[], width = 100, height = 32, padding = 3): string {
    const line = this.sparklinePath(data, width, height, padding);
    if (!line) return '';
    return `${line} L ${(width - padding).toFixed(1)} ${(height - padding).toFixed(1)} L ${padding} ${(height - padding).toFixed(1)} Z`;
  }

  companyCountSeries(field: 'totalCompanies' | 'activeCompanies' | 'trialCompanies' | 'suspendedCompanies'): number[] {
    return this.platformMetricsHistory.map(p => p[field]);
  }

  // ---------- PLATFORM: revenue trend chart (single series, auto-scaled) ----------

  get revenueSeries(): number[] {
    return this.platformMetricsHistory.map(p => Number(p.revenue) || 0);
  }

  get revenueChartMax(): number {
    const max = Math.max(...this.revenueSeries, 0);
    return max > 0 ? Math.ceil(max * 1.2) : 100;
  }

  revenueYLabels(): number[] {
    const max = this.revenueChartMax;
    return [max, max * 0.75, max * 0.5, max * 0.25, 0];
  }

  revenueLinePath(): string {
    return this.chartPath(this.revenueSeries, this.revenueChartMax, 520, 160, 25);
  }

  revenueAreaPath(): string {
    const line = this.revenueLinePath();
    if (!line) return '';
    const width = 520, padding = 25, height = 160;
    return `${line} L ${width - padding} ${height} L ${padding} ${height} Z`;
  }

  private chartPath(data: number[], max: number, width: number, height: number, padding: number): string {
    if (!data || data.length === 0) return '';
    const range = max || 1;
    const stepX = data.length > 1 ? (width - padding * 2) / (data.length - 1) : 0;
    return data.map((v, i) => {
      const x = padding + i * stepX;
      const y = height - (v / range) * height;
      return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
    }).join(' ');
  }

  /** A handful of evenly-spaced date labels along the x-axis (not one per day - a
   * 30-90 point series would collide badly), formatted like "Jun 18". */
  revenueXLabels(): { x: number; label: string }[] {
    const points = this.platformMetricsHistory;
    if (points.length === 0) return [];
    const width = 520, padding = 25;
    const stepX = points.length > 1 ? (width - padding * 2) / (points.length - 1) : 0;
    const maxLabels = 7;
    const labelEvery = Math.max(1, Math.ceil(points.length / maxLabels));
    const labels: { x: number; label: string }[] = [];
    for (let i = 0; i < points.length; i += labelEvery) {
      const d = new Date(points[i].date);
      labels.push({ x: padding + i * stepX, label: d.toLocaleString('en-US', { month: 'short', day: 'numeric' }) });
    }
    return labels;
  }

  get platformOverviewRows(): { icon: string; label: string; value: number; pct: number | null; bg: string; fg: string }[] {
    const s = this.platformSummary;
    if (!s) return [];
    const total = s.totalCompanies || 0;
    const pct = (n: number) => total > 0 ? Math.round((n / total) * 10000) / 100 : 0;
    return [
      { icon: 'bi-buildings', label: 'Total Companies', value: s.totalCompanies, pct: total > 0 ? 100 : 0, bg: '#EEF2FF', fg: '#4F46E5' },
      { icon: 'bi-check-circle', label: 'Active Companies', value: s.activeCompanies, pct: pct(s.activeCompanies), bg: '#d1fae5', fg: '#10b981' },
      { icon: 'bi-hourglass-split', label: 'On Trial', value: s.trialCompanies, pct: pct(s.trialCompanies), bg: '#dbeafe', fg: '#0ea5e9' },
      { icon: 'bi-pause-circle', label: 'Suspended', value: s.suspendedCompanies, pct: pct(s.suspendedCompanies), bg: '#ffedd5', fg: '#f97316' },
      { icon: 'bi-clock-history', label: 'Pending Verification', value: s.pendingVerificationCompanies, pct: pct(s.pendingVerificationCompanies), bg: '#f1f5f9', fg: '#475569' },
      { icon: 'bi-people', label: 'Total Platform Users', value: s.totalPlatformUsers, pct: null, bg: '#e0e7ff', fg: '#4338ca' },
    ];
  }

  getDonutSegment(index: number): { stroke: string; dashArray: string; dashOffset: number } {
    const s = this.summary;
    if (!s) return { stroke: '#e2e8f0', dashArray: '0 377', dashOffset: 0 };
    
    const pending = s.serviceDeskPendingCount || 0;
    const inProgress = s.serviceDeskInProgressCount || 0;
    const resolved = s.serviceDeskResolvedCount || 0;
    const onHold = s.serviceDeskOnHoldCount || 0;
    
    const total = pending + inProgress + resolved + onHold;
    if (total === 0) {
      if (index === 0) return { stroke: '#e2e8f0', dashArray: '377 377', dashOffset: 0 };
      return { stroke: '#e2e8f0', dashArray: '0 377', dashOffset: 0 };
    }
    
    const values = [pending, inProgress, resolved, onHold];
    const colors = ['#f59e0b', '#0284c7', '#10b981', '#6b7280'];
    
    const c = 377;
    let accumulatedPercent = 0;
    
    for (let i = 0; i < index; i++) {
      accumulatedPercent += (values[i] / total) * 100;
    }
    
    const percent = (values[index] / total) * 100;
    const dashArray = `${(percent * c) / 100} ${c}`;
    const dashOffset = -((accumulatedPercent * c) / 100);
    
    return {
      stroke: colors[index],
      dashArray,
      dashOffset
    };
  }

  formatCurrency(val: number | undefined): string {
    if (val == null) return 'BDT 0';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'BDT',
      currencyDisplay: 'code',
      maximumFractionDigits: 0,
    }).format(val);
  }
}
