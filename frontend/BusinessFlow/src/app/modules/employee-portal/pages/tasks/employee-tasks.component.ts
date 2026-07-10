import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeePortalService, TaskItem } from '../../services/employee-portal.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-employee-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <div>
        <h4 class="page-title">My Tasks</h4>
        <p class="page-subtitle">View and update your assigned tasks.</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="card border-0 shadow-sm p-3 mb-4">
      <div class="d-flex gap-2 flex-wrap">
        <button class="btn btn-sm" [class.btn-primary]="filter === 'ALL'" [class.btn-outline-secondary]="filter !== 'ALL'" (click)="filter = 'ALL'">All ({{ allTasks.length }})</button>
        <button class="btn btn-sm" [class.btn-primary]="filter === 'PENDING'" [class.btn-outline-secondary]="filter !== 'PENDING'" (click)="filter = 'PENDING'">Pending ({{ countByStatus('PENDING') }})</button>
        <button class="btn btn-sm" [class.btn-primary]="filter === 'IN_PROGRESS'" [class.btn-outline-secondary]="filter !== 'IN_PROGRESS'" (click)="filter = 'IN_PROGRESS'">In Progress ({{ countByStatus('IN_PROGRESS') }})</button>
        <button class="btn btn-sm" [class.btn-primary]="filter === 'COMPLETED'" [class.btn-outline-secondary]="filter !== 'COMPLETED'" (click)="filter = 'COMPLETED'">Completed ({{ countByStatus('COMPLETED') }})</button>
      </div>
    </div>

    @if (loading) {
      <div class="text-center py-5"><div class="spinner-border text-primary"></div></div>
    } @else if (filteredTasks.length) {
      <div class="row g-3">
        @for (task of filteredTasks; track task.id) {
          <div class="col-md-6 col-lg-4">
            <div class="card border-0 shadow-sm h-100 task-card">
              <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <h6 class="fw-bold mb-0">{{ task.title }}</h6>
                  <span class="badge" [class]="getPriorityClass(task.priority)">{{ task.priority }}</span>
                </div>
                @if (task.description) {
                  <p class="text-muted small mb-3">{{ task.description | slice:0:100 }}{{ task.description.length > 100 ? '...' : '' }}</p>
                }
                <div class="d-flex align-items-center gap-2 mb-3">
                  <span class="badge" [class]="getStatusClass(task.status)">{{ task.status.replace('_', ' ') }}</span>
                  @if (task.dueDate) {
                    <small class="text-muted">Due: {{ task.dueDate | date:'mediumDate' }}</small>
                  }
                </div>
                <div class="d-flex gap-2">
                  @if (task.status !== 'COMPLETED') {
                    <select class="form-select form-select-sm" [ngModel]="task.status" (ngModelChange)="updateStatus(task, $event)" style="width: auto">
                      <option value="PENDING">Pending</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="BLOCKED">Blocked</option>
                    </select>
                  }
                </div>
              </div>
            </div>
          </div>
        }
      </div>
    } @else {
      <div class="text-center py-5 text-muted">
        <i class="bi bi-check-circle" style="font-size: 3rem; opacity: 0.3"></i>
        <p class="mt-3">No tasks found.</p>
      </div>
    }
  `,
  styles: [`
    .task-card { border-radius: 12px; transition: all 0.2s; }
    .task-card:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(0,0,0,0.1) !important; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; }
    .page-subtitle { color: #64748b; margin: 0; }
  `]
})
export class EmployeeTasksComponent implements OnInit {
  private empService = inject(EmployeePortalService);
  allTasks: TaskItem[] = [];
  filteredTasks: TaskItem[] = [];
  filter = 'ALL';
  loading = true;
  private userId: number | null = null;

  ngOnInit(): void {
    this.empService.getMyProfile().subscribe({
      next: (p) => {
        this.userId = p.userId;
        this.empService.getAllTasks(0, 100).subscribe({
          next: (res: any) => {
            const tasks = res.content || res || [];
            this.allTasks = tasks.filter((t: any) => t.assigneeId === this.userId);
            this.applyFilter();
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }

  applyFilter(): void {
    this.filteredTasks = this.filter === 'ALL' ? this.allTasks : this.allTasks.filter(t => t.status === this.filter);
  }

  countByStatus(status: string): number {
    return this.allTasks.filter(t => t.status === status).length;
  }

  updateStatus(task: TaskItem, newStatus: string): void {
    this.empService.updateTask(task.id, { status: newStatus }).subscribe({
      next: () => {
        task.status = newStatus;
        this.applyFilter();
      }
    });
  }

  getStatusClass(s: string): string {
    const m: Record<string, string> = { PENDING: 'bg-warning', IN_PROGRESS: 'bg-info', COMPLETED: 'bg-success', BLOCKED: 'bg-danger', CANCELLED: 'bg-secondary' };
    return m[s] || 'bg-secondary';
  }

  getPriorityClass(p: string): string {
    const m: Record<string, string> = { LOW: 'bg-secondary', NORMAL: 'bg-primary', HIGH: 'bg-warning', URGENT: 'bg-danger' };
    return m[p] || 'bg-secondary';
  }
}
