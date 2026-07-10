import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AttendanceRecord } from '../../models/attendance.model';
import { AttendanceService } from '../../services/attendance.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-attendance-list',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './attendance-list.html',
})
export class AttendanceList implements OnInit {
  records: AttendanceRecord[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  statusFilter = '';
  statuses = ['PRESENT', 'ABSENT', 'LATE', 'LEAVE', 'HALF_DAY'];

  constructor(private attendanceService: AttendanceService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    const obs = this.statusFilter
      ? this.attendanceService.listByStatus(this.statusFilter, this.page)
      : this.attendanceService.list(this.page);
    obs.subscribe({
      next: (res) => {
        this.records = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load records';
        this.loading = false;
      },
    });
  }

  approve(r: AttendanceRecord): void {
    this.attendanceService.approve(r.id).subscribe({
      next: () => {
        this.success = 'Approved';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  statusClass(s: string): string {
    return (
      {
        PRESENT: 'text-bg-success',
        LATE: 'text-bg-warning',
        ABSENT: 'text-bg-danger',
        LEAVE: 'text-bg-info',
        HALF_DAY: 'text-bg-secondary',
      }[s] || 'text-bg-light'
    );
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
