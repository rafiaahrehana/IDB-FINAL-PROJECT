import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService, DashboardSummary, AnnouncementSummary } from '../../../core/services/dashboard.service';
import { AiService } from '../../../core/services/ai.service';
import { ServiceRequestService } from '../../../modules/servicedesk/services/service-request.service';
import { ServiceRequest } from '../../../modules/servicedesk/models/servicedesk.model';
import { MeetingService } from '../../../modules/meeting/services/meeting.service';
import { Meeting } from '../../../modules/meeting/models/meeting.model';
import { CompanyService } from '../../../modules/platform-admin/services/company.service';
import { Company } from '../../../modules/platform-admin/models/platform-admin.model';
import { PlatformUserService } from '../../../modules/platform-admin/services/platform-user.service';
import { Loader } from '../../../shared/components/loader/loader';

interface Kpi {
  label: string;
  value: string;
  sub?: string;
}

interface Bar {
  label: string;
  value: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, Loader],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="page-header">
      <div>
        <h4 class="page-title">
          @if (isPlatform) { Platform Dashboard }
          @else { Dashboard }
        </h4>
        <p class="page-subtitle">Welcome back, {{ (auth.currentUser$ | async)?.firstName || 'there' }}.</p>
      </div>
      @if (!isPlatform) {
      <button class="btn btn-primary" (click)="generateInsights()" [disabled]="insightsLoading">
        @if (insightsLoading) { <span class="spinner-border spinner-border-sm me-1"></span> }
        Generate Insights
      </button>
      }
    </div>

    @if (loading) {
      <app-loader />
    }

    <!-- ========== PLATFORM DASHBOARD (SUPER_ADMIN / SYSTEM_ADMIN / etc.) ========== -->
    @if (isPlatform && !loading) {
      <!-- Platform Stats -->
      <div class="row g-3 mb-4">
        <div class="col-md-3">
          <div class="card border-0 shadow-sm p-4 platform-stat">
            <div class="d-flex align-items-center gap-3">
              <div class="platform-stat-icon bg-primary bg-opacity-10 text-primary"><i class="bi bi-buildings"></i></div>
              <div>
                <h4 class="fw-bold mb-0">{{ platformCompanyCount }}</h4>
                <small class="text-muted">Total Companies</small>
              </div>
            </div>
            <a routerLink="/platform/companies" class="stretched-link"></a>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card border-0 shadow-sm p-4 platform-stat">
            <div class="d-flex align-items-center gap-3">
              <div class="platform-stat-icon bg-success bg-opacity-10 text-success"><i class="bi bi-person-badge"></i></div>
              <div>
                <h4 class="fw-bold mb-0">{{ platformUserCount }}</h4>
                <small class="text-muted">Platform Users</small>
              </div>
            </div>
            <a routerLink="/platform/platform-users" class="stretched-link"></a>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card border-0 shadow-sm p-4 platform-stat">
            <div class="d-flex align-items-center gap-3">
              <div class="platform-stat-icon bg-info bg-opacity-10 text-info"><i class="bi bi-headset"></i></div>
              <div>
                <h4 class="fw-bold mb-0">{{ openSupportTickets }}</h4>
                <small class="text-muted">Open Support Tickets</small>
              </div>
            </div>
            <a routerLink="/support/tickets" class="stretched-link"></a>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card border-0 shadow-sm p-4 platform-stat">
            <div class="d-flex align-items-center gap-3">
              <div class="platform-stat-icon bg-warning bg-opacity-10 text-warning"><i class="bi bi-cash-coin"></i></div>
              <div>
                <h4 class="fw-bold mb-0 text-success">Active</h4>
                <small class="text-muted">System Status</small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Platform Quick Actions -->
      <div class="card border-0 shadow-sm p-4 mb-4">
        <h6 class="fw-bold mb-3"><i class="bi bi-lightning text-warning me-2"></i>Platform Management</h6>
        <div class="row g-2">
          <div class="col-auto">
            <a routerLink="/platform/companies" class="btn btn-outline-primary btn-sm"><i class="bi bi-buildings me-1"></i>Companies</a>
          </div>
          <div class="col-auto">
            <a routerLink="/platform/platform-users" class="btn btn-outline-primary btn-sm"><i class="bi bi-person-badge me-1"></i>Users</a>
          </div>
          <div class="col-auto">
            <a routerLink="/platform/custom-roles" class="btn btn-outline-primary btn-sm"><i class="bi bi-shield me-1"></i>Roles</a>
          </div>
          <div class="col-auto">
            <a routerLink="/platform/feature-flags" class="btn btn-outline-primary btn-sm"><i class="bi bi-flag me-1"></i>Features</a>
          </div>
          <div class="col-auto">
            <a routerLink="/support/tickets" class="btn btn-outline-primary btn-sm"><i class="bi bi-headset me-1"></i>Support</a>
          </div>
          <div class="col-auto">
            <a routerLink="/platform/platform-expenses" class="btn btn-outline-primary btn-sm"><i class="bi bi-cash-stack me-1"></i>Expenses</a>
          </div>
          <div class="col-auto">
            <a routerLink="/platform/locations" class="btn btn-outline-primary btn-sm"><i class="bi bi-geo-alt me-1"></i>Locations</a>
          </div>
        </div>
      </div>

      <!-- Support Overview -->
      <div class="row g-3 mb-4">
        <div class="col-lg-6">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header py-2 d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Recent Support Tickets</span>
              <a routerLink="/support/tickets" class="small">View all</a>
            </div>
            <div class="card-body p-0">
              <table class="table align-middle mb-0">
                <thead><tr><th>Ticket</th><th>Priority</th><th>Status</th></tr></thead>
                <tbody>
                  @if (recentTickets.length === 0) {
                    <tr><td colspan="3" class="text-center text-muted py-4">No tickets</td></tr>
                  }
                  @for (t of recentTickets; track t.id) {
                    <tr>
                      <td class="small fw-medium">{{ t.ticketNumber || ('#' + t.id) }}</td>
                      <td><span class="badge" [class]="getPriorityBadge(t.priority)">{{ t.priority }}</span></td>
                      <td><span class="badge text-bg-light border">{{ t.status }}</span></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="col-lg-6">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header py-2"><span class="fw-semibold">Recent Activity</span></div>
            <div class="card-body">
              @if (recentTickets.length === 0 && announcements.length === 0) {
                <p class="text-muted small mb-0">No recent activity</p>
              }
              @for (a of announcements; track a.id) {
                <div class="mb-3">
                  <div class="fw-semibold small">{{ a.title }}</div>
                  <div class="text-muted small">{{ a.body }}</div>
                </div>
              }
              @if (announcements.length === 0 && recentTickets.length > 0) {
                @for (t of recentTickets.slice(0, 3); track t.id) {
                  <div class="d-flex align-items-center gap-2 mb-2">
                    <span class="badge text-bg-light border">{{ t.ticketNumber }}</span>
                    <span class="small text-truncate" style="max-width: 200px">{{ t.title }}</span>
                  </div>
                }
              }
            </div>
          </div>
        </div>
      </div>
    }

    <!-- ========== COMPANY DASHBOARD (COMPANY_OWNER / EMPLOYEE) ========== -->
    @if (!isPlatform && !loading) {
      <!-- Subscription Widget -->
      @if (myCompany) {
        <div class="card mb-4" [class.border-success]="myCompany.status === 'ACTIVE'" [class.border-warning]="myCompany.status === 'SUSPENDED'" [class.border-danger]="myCompany.status === 'DEACTIVATED'">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <div class="d-flex align-items-center gap-3">
                <div class="subscription-icon bg-primary bg-opacity-10 text-primary">
                  <i class="bi bi-credit-card-2-front"></i>
                </div>
                <div>
                  <h6 class="fw-bold mb-1">{{ myCompany.subscriptionPlan }} Plan</h6>
                  <small class="text-muted">Status: <span class="badge" [class.bg-success]="myCompany.status === 'ACTIVE'" [class.bg-warning]="myCompany.status === 'SUSPENDED'" [class.bg-danger]="myCompany.status === 'DEACTIVATED'">{{ myCompany.status }}</span></small>
                  @if (myCompany.subscriptionEnd) {
                    <small class="text-muted ms-2">Expires: {{ myCompany.subscriptionEnd }}</small>
                  }
                </div>
              </div>
              <div class="d-flex gap-2">
                <a routerLink="/company/subscription" class="btn btn-outline-primary btn-sm">
                  @if (myCompany.subscriptionPlan === 'FREE') { Subscribe } @else { Manage Plan }
                </a>
                @if (myCompany.status === 'ACTIVE') {
                  <a routerLink="/company/settings" class="btn btn-outline-secondary btn-sm">Settings</a>
                }
              </div>
            </div>
          </div>
        </div>
      }

      <!-- KPI cards -->
      <div class="kpi-grid mb-4">
        @for (kpi of kpis; track kpi.label) {
          <div class="kpi-card">
            <p class="kpi-label">{{ kpi.label }}</p>
            <p class="kpi-value">{{ kpi.value }}</p>
            @if (kpi.sub) { <p class="kpi-sub">{{ kpi.sub }}</p> }
          </div>
        }
      </div>

      <!-- Charts row -->
      <div class="row g-3 mb-4">
        <div class="col-lg-6">
          <div class="card h-100">
            <div class="card-header py-2"><span class="fw-semibold">Task Status</span></div>
            <div class="card-body">
              @for (b of taskBars; track b.label) {
                <div class="bar-row">
                  <span class="bar-label">{{ b.label }}</span>
                  <span class="bar-track"><span class="bar-fill" [style.width.%]="barPct(b.value)"></span></span>
                  <span class="bar-value">{{ b.value }}</span>
                </div>
              }
            </div>
          </div>
        </div>
        <div class="col-lg-6">
          <div class="card h-100">
            <div class="card-header py-2 d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Project Progress</span>
              <span class="badge text-bg-primary">{{ activeProjects }} active</span>
            </div>
            <div class="card-body">
              <div class="d-flex justify-content-between mb-1">
                <span class="small text-muted">Average completion</span>
                <span class="small fw-semibold">{{ projectProgress }}%</span>
              </div>
              <div class="progress" style="height: 10px;">
                <div class="progress-bar bg-primary" [style.width.%]="projectProgress"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Lists row -->
      <div class="row g-3">
        <div class="col-lg-4">
          <div class="card h-100">
            <div class="card-header py-2 d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Recent Requests</span>
              <a routerLink="/servicedesk/requests" class="small">View all</a>
            </div>
            <div class="card-body p-0">
              <table class="table align-middle mb-0">
                <thead><tr><th>Subject</th><th>Status</th></tr></thead>
                <tbody>
                  @if (recentRequests.length === 0) {
                    <tr><td colspan="2" class="text-center text-muted py-4">No requests</td></tr>
                  }
                  @for (r of recentRequests; track r.id) {
                    <tr>
                      <td>{{ r.title || ('#' + r.id) }}</td>
                      <td><span class="badge text-bg-light border">{{ r.status }}</span></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="card h-100">
            <div class="card-header py-2"><span class="fw-semibold">Announcements</span></div>
            <div class="card-body">
              @if (announcements.length === 0) {
                <p class="text-muted small mb-0">No announcements</p>
              }
              @for (a of announcements; track a.id) {
                <div class="mb-3">
                  <div class="fw-semibold small">{{ a.title }}</div>
                  <div class="text-muted small">{{ a.body }}</div>
                  @if (a.createdByName) { <div class="text-muted" style="font-size: 0.75rem;">— {{ a.createdByName }}</div> }
                </div>
              }
            </div>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="card h-100">
            <div class="card-header py-2 d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Today's Meetings</span>
              <a routerLink="/meeting" class="small">Calendar</a>
            </div>
            <div class="card-body">
              @if (todaysMeetings.length === 0) {
                <p class="text-muted small mb-0">No meetings today</p>
              }
              @for (m of todaysMeetings; track m.id) {
                <div class="d-flex align-items-center gap-2 mb-2">
                  <span class="badge text-bg-light border">{{ timeOf(m.startTime) }}</span>
                  <span class="small">{{ m.title }}</span>
                </div>
              }
            </div>
          </div>
        </div>
      </div>

      <!-- AI recommendations -->
      <div class="card mt-4">
        <div class="card-header py-2"><span class="fw-semibold"><i class="bi bi-stars me-1"></i>Smart Recommendations</span></div>
        <div class="card-body">
          @if (recommendations.length === 0) {
            <p class="text-muted small mb-0">No recommendations right now.</p>
          }
          <ul class="list-group list-group-flush">
            @for (rec of recommendations; track rec.message) {
              <li class="list-group-item px-0 py-2 d-flex justify-content-between align-items-center border-0">
                <span class="small">
                  <span class="badge me-2"
                    [class.text-bg-danger]="rec.severity === 'CRITICAL'"
                    [class.text-bg-warning]="rec.severity === 'WARNING'"
                    [class.text-bg-info]="rec.severity === 'INFO'">{{ rec.severity }}</span>
                  {{ rec.message }}
                </span>
                @if (rec.link) { <a [routerLink]="rec.link" class="btn btn-outline-primary btn-sm">Open</a> }
              </li>
            }
          </ul>
          @if (insights) {
            <hr />
            <div class="small" style="white-space: pre-wrap">{{ insights }}</div>
          }
          @if (insightsError) {
            <div class="small text-danger">{{ insightsError }}</div>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; }
    .page-subtitle { color: #64748b; margin: 0; }
    .platform-stat { position: relative; transition: transform 0.15s; cursor: pointer; }
    .platform-stat:hover { transform: translateY(-2px); }
    .platform-stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; flex-shrink: 0; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 1rem; }
    .kpi-card { background: #fff; border-radius: 12px; padding: 1.25rem; border: 1px solid #e2e8f0; }
    .kpi-label { font-size: 0.8rem; color: #64748b; margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.03em; }
    .kpi-value { font-size: 1.75rem; font-weight: 700; margin: 0; color: #0f172a; }
    .kpi-sub { font-size: 0.75rem; color: #94a3b8; margin: 0.25rem 0 0; }
    .bar-row { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem; }
    .bar-label { width: 90px; font-size: 0.8rem; color: #64748b; }
    .bar-track { flex: 1; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; }
    .bar-fill { display: block; height: 100%; background: #2563EB; border-radius: 4px; transition: width 0.4s ease; }
    .bar-value { width: 30px; font-size: 0.8rem; font-weight: 600; text-align: right; }
    .subscription-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; flex-shrink: 0; }
  `],
})
export class DashboardComponent implements OnInit {
  loading = false;
  isPlatform = false;
  insights = '';
  insightsLoading = false;
  insightsError = '';
  recommendations: any[] = [];
  myCompany?: Company;

  kpis: Kpi[] = [];
  taskBars: Bar[] = [];
  maxTask = 1;
  announcements: AnnouncementSummary[] = [];
  recentRequests: ServiceRequest[] = [];
  todaysMeetings: Meeting[] = [];
  projectProgress = 0;
  activeProjects = 0;

  // Platform stats
  platformCompanyCount = 0;
  platformUserCount = 0;
  openSupportTickets = 0;
  recentTickets: any[] = [];

  constructor(
    public auth: AuthService,
    private dashboardService: DashboardService,
    private aiService: AiService,
    private requestService: ServiceRequestService,
    private meetingService: MeetingService,
    private companyService: CompanyService,
    private platformUserService: PlatformUserService,
  ) {
    this.isPlatform = this.auth.hasAnyRole(this.platformRoles);
  }

  private readonly platformRoles = ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'];

  ngOnInit(): void {
    if (this.isPlatform) {
      this.loadPlatformDashboard();
    } else {
      this.loadCompanyDashboard();
    }
  }

  private loadPlatformDashboard(): void {
    this.loading = true;
    this.companyService.list(0, 1).subscribe({
      next: (p) => { this.platformCompanyCount = p.totalElements; },
    });
    this.platformUserService.list(0, 1).subscribe({
      next: (p) => { this.platformUserCount = p.totalElements; },
    });
    this.requestService.list(0, 5).subscribe({
      next: (res) => {
        const content = res?.content || [];
        this.openSupportTickets = content.length;
        this.recentTickets = content;
      },
      complete: () => { this.loading = false; },
    });
  }

  private loadCompanyDashboard(): void {
    this.loading = true;
    this.companyService.getMyCompany().subscribe({
      next: (company) => (this.myCompany = company),
      error: () => {},
    });
    this.dashboardService.getSummary().subscribe({
      next: (s: DashboardSummary) => {
        this.loading = false;
        this.kpis = [
          { label: 'Employees', value: String(s.totalEmployees) },
          { label: 'Clients', value: String(s.totalClients) },
          { label: 'Projects', value: String(s.activeProjects) },
          { label: 'Revenue', value: this.fmt(s.outstandingInvoiceAmount) },
          { label: 'Pending Tasks', value: String(s.openTasks) },
          { label: "Today's Meetings", value: String(s.meetingsToday) },
        ];
        this.taskBars = [
          { label: 'Pending', value: s.taskPending },
          { label: 'In Progress', value: s.taskInProgress },
          { label: 'Completed', value: s.taskCompleted },
          { label: 'Blocked', value: s.taskBlocked },
          { label: 'Cancelled', value: s.taskCancelled },
        ];
        this.maxTask = Math.max(1, ...this.taskBars.map((b) => b.value));
        this.projectProgress = Math.round(s.projectProgress);
        this.activeProjects = s.activeProjects;
        this.announcements = s.announcements || [];
      },
      error: () => (this.loading = false),
    });
    this.aiService.recommendations().subscribe({
      next: (recs) => (this.recommendations = recs),
    });
    this.requestService.list(0, 5).subscribe({
      next: (p) => (this.recentRequests = (p && p.content) || []),
    });
    this.meetingService.list(0, 50).subscribe({
      next: (p) => {
        const today = new Date().toISOString().slice(0, 10);
        this.todaysMeetings = ((p && p.content) || []).filter(
          (m: Meeting) => m.startTime && m.startTime.slice(0, 10) === today,
        );
      },
    });
  }

  barPct(value: number): number {
    return Math.round((value / this.maxTask) * 100);
  }

  timeOf(iso?: string): string {
    if (!iso) return '';
    return iso.slice(11, 16);
  }

  getPriorityBadge(priority: string): string {
    const m: Record<string, string> = {
      CRITICAL: 'bg-danger', HIGH: 'bg-warning text-dark',
      MEDIUM: 'bg-info', LOW: 'bg-secondary'
    };
    return m[priority] || 'bg-secondary';
  }

  generateInsights(): void {
    this.insightsLoading = true;
    this.insightsError = '';
    this.aiService.insights().subscribe({
      next: (res) => {
        this.insights = res.insights;
        this.insightsLoading = false;
      },
      error: (err) => {
        this.insightsError =
          err?.error?.message || 'Insights failed - check your AI provider config';
        this.insightsLoading = false;
      },
    });
  }

  private fmt(n: number | undefined): string {
    if (n == null) return '-';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(n);
  }
}
