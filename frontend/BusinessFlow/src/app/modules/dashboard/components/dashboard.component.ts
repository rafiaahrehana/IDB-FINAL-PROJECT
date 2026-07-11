import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService, DashboardSummary, PlatformSummary, ClientSummary } from '../../../core/services/dashboard.service';
import { AiService } from '../../../core/services/ai.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { RbacService } from '../../../core/services/rbac.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  readonly loading = signal(true);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly platformSummary = signal<PlatformSummary | null>(null);
  readonly clientSummary = signal<ClientSummary | null>(null);
  readonly recommendations = signal<any[]>([]);
  readonly insights = signal('');
  readonly insightsLoading = signal(false);
  readonly realtimeMetrics = signal<any>(null);

  readonly isCompanyRole = signal(false);
  readonly isPlatformRole = signal(false);
  readonly isClientRole = signal(false);
  readonly canManagePlatform = signal(false);
  readonly quickLinks = signal<any[]>([]);

  constructor(
    public auth: AuthService,
    public rbac: RbacService,
    private dashboardService: DashboardService,
    private aiService: AiService,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    const roles: string[] = user?.roles || [];

    this.isCompanyRole.set(roles.some((r: string) => ['COMPANY_OWNER', 'EMPLOYEE'].includes(r)));
    this.isClientRole.set(roles.includes('CLIENT'));
    this.isPlatformRole.set(!this.isCompanyRole() && !this.isClientRole() && roles.length > 0);
    this.canManagePlatform.set(roles.some((r: string) => ['SUPER_ADMIN', 'SYSTEM_ADMIN'].includes(r)));

    if (this.isCompanyRole()) {
      this.loadCompanyDashboard();
    } else if (this.isPlatformRole()) {
      this.quickLinks.set(this.buildPlatformQuickLinks(roles));
      this.loadPlatformDashboard();
    } else if (this.isClientRole()) {
      this.quickLinks.set(this.buildClientQuickLinks());
      this.loadClientDashboard();
    }

    this.connectRealtimeMetrics();
  }

  private connectRealtimeMetrics(): void {
    this.ws.subscribe('/v1/metrics/stream').subscribe({
      next: (data: any) => this.realtimeMetrics.set(data),
      error: () => {},
    });
  }

  private loadCompanyDashboard(): void {
    this.loading.set(true);
    this.dashboardService.getSummary().subscribe({
      next: (s) => { this.summary.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.aiService.recommendations().subscribe({
      next: (recs) => this.recommendations.set(recs),
    });
  }

  generateInsights(): void {
    this.insightsLoading.set(true);
    this.aiService.insights().subscribe({
      next: (res) => { this.insights.set(res.insights); this.insightsLoading.set(false); },
      error: () => { this.insights.set('Failed to generate insights'); this.insightsLoading.set(false); },
    });
  }

  private loadPlatformDashboard(): void {
    this.loading.set(true);
    this.dashboardService.getPlatformSummary().subscribe({
      next: (s) => { this.platformSummary.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  private loadClientDashboard(): void {
    this.loading.set(true);
    this.dashboardService.getClientSummary().subscribe({
      next: (s) => { this.clientSummary.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  private buildPlatformQuickLinks(roles: string[]): any[] {
    const links: any[] = [];
    if (this.canManagePlatform()) {
      links.push(
        { label: 'Companies', description: 'Manage tenant companies', icon: 'building-2', link: '/platform/companies' },
        { label: 'Platform Users', description: 'SaaS staff accounts', icon: 'shield', link: '/platform/platform-users' },
        { label: 'Feature Flags', description: 'Toggle features', icon: 'flag', link: '/platform/feature-flags' },
        { label: 'Custom Roles', description: 'Permission sets', icon: 'shield-check', link: '/platform/custom-roles' },
      );
    }
    if (roles.includes('SUPPORT_AGENT') || roles.includes('SUPPORT_MANAGER')) {
      links.push(
        { label: 'Support Tickets', description: 'Resolve tenant tickets', icon: 'messages-square', link: '/support/tickets' },
        { label: 'SLA Policies', description: 'Response targets', icon: 'timer', link: '/support/sla-policies' },
      );
    }
    links.push(
      { label: 'Notifications', description: 'Latest updates', icon: 'bell', link: '/notifications' },
      { label: 'Settings', description: 'Profile & preferences', icon: 'settings', link: '/notifications/preferences' },
    );
    return links;
  }

  private buildClientQuickLinks(): any[] {
    return [
      { label: 'Service Requests', description: 'Track your requests', icon: 'clipboard-check', link: '/servicedesk/requests' },
      { label: 'Knowledge Base', description: 'Guides and answers', icon: 'book-open', link: '/servicedesk/kb' },
      { label: 'Notifications', description: 'Latest updates', icon: 'bell', link: '/notifications' },
      { label: 'Settings', description: 'Profile & preferences', icon: 'settings', link: '/notifications/preferences' },
    ];
  }

  fmt(n: number | undefined): string {
    if (n == null) return '-';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(n);
  }

  iconClass(icon: string): string {
    const map: Record<string, string> = {
      'building-2': 'bi bi-building', 'building': 'bi bi-building',
      'shield': 'bi bi-shield', 'shield-check': 'bi bi-shield-check',
      'shield-alert': 'bi bi-shield-exclamation',
      'flag': 'bi bi-flag', 'bell': 'bi bi-bell',
      'messages-square': 'bi bi-chat-square-text',
      'timer': 'bi bi-stopwatch', 'settings': 'bi bi-gear',
      'clipboard-check': 'bi bi-clipboard-check',
      'book-open': 'bi bi-book', 'users': 'bi bi-people',
      'kanban': 'bi bi-kanban', 'trending-up': 'bi bi-graph-up',
      'clock': 'bi bi-clock', 'loader': 'bi bi-arrow-repeat',
      'receipt': 'bi bi-receipt', 'wallet': 'bi bi-wallet2',
      'banknote': 'bi bi-cash', 'lightbulb': 'bi bi-lightbulb',
      'check-circle': 'bi bi-check-circle', 'pause-circle': 'bi bi-pause-circle',
    };
    return map[icon] || 'bi bi-grid';
  }

  recIconClass(severity: string): string {
    if (severity === 'CRITICAL') return 'bi bi-exclamation-circle';
    if (severity === 'WARNING') return 'bi bi-exclamation-triangle';
    return 'bi bi-info-circle';
  }
}
