import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeePortalService, LeaveBalance, LeaveRequest } from '../../services/employee-portal.service';

@Component({
  selector: 'app-employee-leaves',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header d-flex justify-content-between align-items-center">
      <div>
        <h4 class="page-title">My Leaves</h4>
        <p class="page-subtitle">Apply for leave and track your leave history.</p>
      </div>
      <button class="btn btn-primary" (click)="showForm = !showForm">
        <i class="bi bi-plus-lg me-1"></i>Apply Leave
      </button>
    </div>

    <!-- Leave Balances -->
    @if (balances.length) {
      <div class="row g-3 mb-4">
        @for (b of balances; track b.leaveType) {
          <div class="col-md-3">
            <div class="card border-0 shadow-sm p-3 text-center">
              <small class="text-muted">{{ b.leaveType }}</small>
              <h4 class="fw-bold my-1" style="color: #2563EB">{{ b.remainingDays }}</h4>
              <small class="text-muted">remaining of {{ b.entitledDays }}</small>
              <div class="progress mt-2" style="height: 4px">
                <div class="progress-bar" [style.width.%]="(b.usedDays / b.entitledDays) * 100"></div>
              </div>
            </div>
          </div>
        }
      </div>
    }

    <!-- Apply Form -->
    @if (showForm) {
      <div class="card border-0 shadow-sm p-4 mb-4">
        <h6 class="fw-bold mb-3">Apply for Leave</h6>
        <form (ngSubmit)="applyLeave()">
          <div class="row g-3">
            <div class="col-md-4">
              <label class="form-label">Leave Type *</label>
              <select class="form-select" [(ngModel)]="form.leaveType" name="leaveType" required>
                <option value="">-- Select --</option>
                <option value="ANNUAL">Annual</option>
                <option value="SICK">Sick</option>
                <option value="CASUAL">Casual</option>
                <option value="MATERNITY">Maternity</option>
                <option value="PATERNITY">Paternity</option>
                <option value="UNPAID">Unpaid</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Start Date *</label>
              <input type="date" class="form-control" [(ngModel)]="form.startDate" name="startDate" required>
            </div>
            <div class="col-md-4">
              <label class="form-label">End Date *</label>
              <input type="date" class="form-control" [(ngModel)]="form.endDate" name="endDate" required>
            </div>
            <div class="col-12">
              <label class="form-label">Reason</label>
              <textarea class="form-control" rows="3" [(ngModel)]="form.reason" name="reason"></textarea>
            </div>
            <div class="col-12">
              <button type="submit" class="btn btn-primary" [disabled]="submitting">
                @if (submitting) { <span class="spinner-border spinner-border-sm me-1"></span> }
                Submit Application
              </button>
              <button type="button" class="btn btn-outline-secondary ms-2" (click)="showForm = false">Cancel</button>
            </div>
          </div>
        </form>
      </div>
    }

    <!-- Leave History -->
    <div class="card border-0 shadow-sm p-4">
      <h6 class="fw-bold mb-3">Leave History</h6>
      @if (loading) {
        <div class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div></div>
      } @else if (leaves.length) {
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead><tr><th>Type</th><th>From</th><th>To</th><th>Days</th><th>Status</th><th>Action</th></tr></thead>
            <tbody>
              @for (l of leaves; track l.id) {
                <tr>
                  <td>{{ l.leaveType }}</td>
                  <td>{{ l.startDate | date:'mediumDate' }}</td>
                  <td>{{ l.endDate | date:'mediumDate' }}</td>
                  <td>{{ l.totalDays }}</td>
                  <td><span class="badge" [class]="getStatusClass(l.status)">{{ l.status }}</span></td>
                  <td>
                    @if (l.status === 'PENDING') {
                      <button class="btn btn-sm btn-outline-danger" (click)="cancelLeave(l.id)">Cancel</button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else {
        <p class="text-muted text-center py-3 mb-0">No leave records found.</p>
      }
    </div>
  `,
  styles: [`
    .progress-bar { background: #2563EB; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; }
    .page-subtitle { color: #64748b; margin: 0; }
  `]
})
export class EmployeeLeavesComponent implements OnInit {
  private empService = inject(EmployeePortalService);
  balances: LeaveBalance[] = [];
  leaves: LeaveRequest[] = [];
  loading = true;
  showForm = false;
  submitting = false;
  form = { leaveType: '', startDate: '', endDate: '', reason: '' };

  ngOnInit(): void {
    this.empService.getMyLeaveBalances().subscribe(b => this.balances = b);
    this.empService.getMyLeaves().subscribe({
      next: (res: any) => { this.leaves = res.content || res || []; this.loading = false; },
      error: () => this.loading = false
    });
  }

  applyLeave(): void {
    this.submitting = true;
    this.empService.applyLeave(this.form).subscribe({
      next: () => {
        this.showForm = false;
        this.submitting = false;
        this.form = { leaveType: '', startDate: '', endDate: '', reason: '' };
        this.empService.getMyLeaves().subscribe((res: any) => this.leaves = res.content || res || []);
        this.empService.getMyLeaveBalances().subscribe(b => this.balances = b);
      },
      error: () => this.submitting = false
    });
  }

  cancelLeave(id: number): void {
    this.empService.cancelLeave(id).subscribe(() => {
      this.leaves = this.leaves.map(l => l.id === id ? { ...l, status: 'CANCELLED' } : l);
    });
  }

  getStatusClass(s: string): string {
    const m: Record<string, string> = { PENDING: 'bg-warning', APPROVED: 'bg-success', REJECTED: 'bg-danger', CANCELLED: 'bg-secondary' };
    return m[s] || 'bg-secondary';
  }
}
