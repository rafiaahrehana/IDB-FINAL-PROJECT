import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EmployeePortalService, EmployeeProfile, LeaveBalance, TaskItem } from '../../services/employee-portal.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-dashboard.component.html',
  styleUrl: './employee-dashboard.component.scss',
})
export class EmployeeDashboardComponent implements OnInit {
  private empService = inject(EmployeePortalService);
  private auth = inject(AuthService);

  profile: EmployeeProfile | null = null;
  leaveBalances: LeaveBalance[] = [];
  myTasks: TaskItem[] = [];
  latestPayroll: any = null;
  daysWorked = 0;
  pendingLeaves = 0;
  loading = true;

  // Role flags
  isOwner = false;
  isEmployee = false;
  isClient = false;

  // Admin stats (COMPANY_OWNER only)
  totalEmployees = 0;
  totalDepartments = 0;
  pendingLeaveRequests: any[] = [];
  recentAnnouncements: any[] = [];

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    this.isOwner = user?.role === 'COMPANY_OWNER';
    this.isEmployee = user?.role === 'EMPLOYEE';
    this.isClient = user?.role === 'CLIENT';

    this.empService.getMyProfile().subscribe({
      next: p => {
        this.profile = p;
        this.loadEmployeeData();
        if (this.isOwner) this.loadAdminData();
      },
      error: () => this.loading = false
    });
  }

  private loadEmployeeData(): void {
    this.empService.getMyLeaveBalances().subscribe(b => {
      this.leaveBalances = b;
      this.pendingLeaves = b.reduce((sum, lb) => sum + lb.pendingDays, 0);
    });

    this.empService.getAllTasks(0, 50).subscribe({
      next: (res: any) => {
        const userId = this.profile?.userId;
        const tasks = res.content || res || [];
        this.myTasks = tasks.filter((t: any) => t.assigneeId === userId);
        this.loading = false;
      },
      error: () => this.loading = false
    });

    this.empService.getMyPayroll(0, 1).subscribe({
      next: (res: any) => {
        const records = res.content || res || [];
        this.latestPayroll = records[0] || null;
      }
    });
  }

  private loadAdminData(): void {
    this.empService.listEmployees(0, 1).subscribe({
      next: (res: any) => { this.totalEmployees = res.totalElements || 0; }
    });

    this.empService.listDepartments().subscribe({
      next: (depts) => { this.totalDepartments = depts.length; }
    });

    this.empService.listPendingLeaves().subscribe({
      next: (res: any) => {
        const data = res.content || res || [];
        this.pendingLeaveRequests = Array.isArray(data) ? data.slice(0, 5) : [];
      }
    });

    this.empService.listAnnouncements().subscribe({
      next: (res: any) => {
        const data = res.content || res || [];
        this.recentAnnouncements = Array.isArray(data) ? data.slice(0, 3) : [];
      }
    });
  }

  reviewLeave(leaveId: number, status: 'APPROVED' | 'REJECTED'): void {
    this.empService.reviewLeave(leaveId, status).subscribe({
      next: () => {
        this.pendingLeaveRequests = this.pendingLeaveRequests.filter(l => l.id !== leaveId);
      }
    });
  }

  getStatusClass(status: string): string {
    const m: Record<string, string> = { PENDING: 'bg-warning', IN_PROGRESS: 'bg-info', COMPLETED: 'bg-success', BLOCKED: 'bg-danger', CANCELLED: 'bg-secondary' };
    return m[status] || 'bg-secondary';
  }

  getPriorityClass(p: string): string {
    const m: Record<string, string> = { LOW: 'bg-secondary', NORMAL: 'bg-primary', HIGH: 'bg-warning text-dark', URGENT: 'bg-danger' };
    return m[p] || 'bg-secondary';
  }

  getLeaveStatusClass(s: string): string {
    const m: Record<string, string> = { PENDING: 'bg-warning text-dark', APPROVED: 'bg-success', REJECTED: 'bg-danger', CANCELLED: 'bg-secondary' };
    return m[s] || 'bg-secondary';
  }
}
