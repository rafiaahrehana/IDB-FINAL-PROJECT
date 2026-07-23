import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ATTENDANCE_STATUSES, AttendanceRecord, ManualAttendanceRequest } from '../../models/attendance.model';
import { AttendanceService } from '../../services/attendance.service';
import { EmployeeService } from '../../../hrm/services/employee.service';
import { Employee } from '../../../hrm/models/hrm.model';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-attendance-list',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, HasPermissionDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './attendance-list.html',
})
export class AttendanceList implements OnInit {
  records: AttendanceRecord[] = [];
  employees: Employee[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  statuses = ATTENDANCE_STATUSES;

  showForm = false;
  saving = false;
  form: Partial<ManualAttendanceRequest> = {};

  constructor(
    private attendanceService: AttendanceService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
    this.employeeService.list(0, 500).subscribe({ next: (res) => { this.employees = res.content; this.cdr.markForCheck(); } });
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    const obs = this.statusFilter
      ? this.attendanceService.listByStatus(this.statusFilter, this.page)
      : this.attendanceService.list(this.page);
    obs.subscribe({
      next: (res) => {
        this.records = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load records';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  approve(r: AttendanceRecord): void {
    this.attendanceService.approve(r.id).subscribe({
      next: () => {
        this.success = 'Approved';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed'; this.cdr.markForCheck(); },
    });
  }

  openAdd(): void {
    this.form = {
      attendanceDate: new Date().toISOString().slice(0, 10),
      status: 'PRESENT',
    };
    this.error = '';
    this.showForm = true;
  }

  setNow(field: 'checkInTime' | 'checkOutTime'): void {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    this.form[field] = `${hh}:${mm}`;
  }

  save(): void {
    if (!this.form.employeeId || !this.form.attendanceDate || !this.form.status) return;
    this.saving = true;
    this.error = '';
    this.attendanceService.createManual(this.form as ManualAttendanceRequest).subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = 'Attendance recorded';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to record attendance';
        this.cdr.markForCheck();
      },
    });
  }

  statusClass(s: string): string {
    return (
      {
        PRESENT: 'text-bg-success',
        LATE: 'text-bg-warning',
        ABSENT: 'text-bg-danger',
        ON_LEAVE: 'text-bg-info',
        HALF_DAY: 'text-bg-secondary',
        WORK_FROM_HOME: 'text-bg-info',
        WEEKEND: 'text-bg-light',
        HOLIDAY: 'text-bg-light',
        PARTIAL_DAY: 'text-bg-warning',
        UNMARKED: 'text-bg-light',
      }[s] || 'text-bg-light'
    );
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
