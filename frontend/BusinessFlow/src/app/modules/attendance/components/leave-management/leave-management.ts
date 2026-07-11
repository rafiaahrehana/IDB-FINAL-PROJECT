import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AttendanceLeave } from '../../models/attendance.model';
import { LeaveService } from '../../services/leave.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-leave-management',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './leave-management.html',
})
export class LeaveManagement implements OnInit {
  leaves: AttendanceLeave[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  showForm = false;
  form: any = {};
  rejectTarget: AttendanceLeave | null = null;
  rejectionReason = '';
  leaveTypes = ['ANNUAL', 'SICK', 'MATERNITY', 'PATERNITY', 'UNPAID', 'EMERGENCY', 'OTHER'];

  constructor(private leaveService: LeaveService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.leaveService.list(this.page).subscribe({
      next: (res) => {
        this.leaves = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load leaves';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  create(): void {
    this.leaveService.create(this.form).subscribe({
      next: () => {
        this.showForm = false;
        this.form = {};
        this.success = 'Leave request submitted';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  approve(l: AttendanceLeave): void {
    this.leaveService.approve(l.id).subscribe({
      next: () => {
        this.success = 'Approved';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  doReject(): void {
    if (!this.rejectTarget) return;
    this.leaveService.reject(this.rejectTarget.id, this.rejectionReason).subscribe({
      next: () => {
        this.rejectTarget = null;
        this.success = 'Rejected';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed';
        this.rejectTarget = null;
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
