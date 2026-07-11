import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LeaveRequest,
  LeaveRequestPayload,
  LeaveRequestStatus,
  LeaveBalance,
  LEAVE_TYPES,
  LEAVE_REQUEST_STATUSES,
} from '../../models/hrm.model';
import { LeaveService } from '../../services/leave.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-leaves',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './leaves.html',
})
export class Leaves implements OnInit {
  // VARIABLES
  requests: LeaveRequest[] = [];
  balances: LeaveBalance[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  view: 'my' | 'all' = 'my';
  statusFilter: LeaveRequestStatus | '' = '';

  showForm = false;
  form: LeaveRequestPayload = { leaveType: 'ANNUAL', startDate: '', endDate: '' };

  reviewTarget: LeaveRequest | null = null;
  reviewAction: 'APPROVED' | 'REJECTED' = 'APPROVED';
  rejectionReason = '';
  cancelTarget: LeaveRequest | null = null;

  leaveTypes = LEAVE_TYPES;
  statuses = LEAVE_REQUEST_STATUSES;

  constructor(private leaveService: LeaveService, private cdr: ChangeDetectorRef) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void {
    this.load();
    this.loadBalances();
  }

  // LOAD LEAVE REQUESTS BASED ON CURRENT VIEW
  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    const req = this.view === 'my'
      ? this.leaveService.listMine(this.page)
      : this.leaveService.list(this.page, 20, this.statusFilter || undefined);
    req.subscribe({
      next: (res) => { this.requests = res.content; this.totalPages = res.totalPages; this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.error = 'Failed to load leave requests'; this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  // LOAD MY BALANCES FOR CURRENT YEAR
  loadBalances(): void {
    this.leaveService.myBalances().subscribe({
      next: (data) => this.balances = data,
      error: () => this.balances = []
    });
  }

  // SWITCH VIEW BETWEEN MY REQUESTS AND ALL REQUESTS
  setView(v: 'my' | 'all'): void {
    if (this.view === v) return;
    this.view = v;
    this.page = 0;
    this.statusFilter = '';
    this.load();
  }

  // OPEN APPLY FORM
  openApply(): void {
    this.form = { leaveType: 'ANNUAL', startDate: '', endDate: '' };
    this.showForm = true;
  }

  // APPLY FOR LEAVE
  apply(): void {
    this.leaveService.apply(this.form).subscribe({
      next: () => { this.success = 'Leave request submitted'; this.showForm = false; this.load(); this.loadBalances(); },
      error: (err) => this.error = err?.error?.message || 'Failed to submit leave request'
    });
  }

  // OPEN REVIEW DIALOG
  openReview(r: LeaveRequest, action: 'APPROVED' | 'REJECTED'): void {
    this.reviewTarget = r;
    this.reviewAction = action;
    this.rejectionReason = '';
  }

  // SUBMIT REVIEW
  doReview(): void {
    if (!this.reviewTarget) return;
    this.leaveService.review(this.reviewTarget.id, {
      status: this.reviewAction,
      rejectionReason: this.reviewAction === 'REJECTED' ? this.rejectionReason : undefined,
    }).subscribe({
      next: () => { this.reviewTarget = null; this.success = 'Leave request reviewed'; this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to review'; this.reviewTarget = null; }
    });
  }

  // CANCEL OWN REQUEST
  doCancel(): void {
    if (!this.cancelTarget) return;
    this.leaveService.cancel(this.cancelTarget.id).subscribe({
      next: () => { this.cancelTarget = null; this.success = 'Leave request cancelled'; this.load(); this.loadBalances(); },
      error: (err) => { this.error = err?.error?.message || 'Cannot cancel'; this.cancelTarget = null; }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // STATUS BADGE CLASS
  statusClass(status: LeaveRequestStatus): string {
    return {
      PENDING: 'text-bg-warning',
      APPROVED: 'text-bg-success',
      REJECTED: 'text-bg-danger',
      CANCELLED: 'text-bg-secondary',
    }[status] || 'text-bg-secondary';
  }
}
