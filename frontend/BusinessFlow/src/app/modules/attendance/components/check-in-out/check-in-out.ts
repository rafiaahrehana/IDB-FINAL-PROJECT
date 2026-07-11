import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AttendanceRecord } from '../../models/attendance.model';
import { AttendanceService } from '../../services/attendance.service';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-check-in-out',
  imports: [CommonModule, FormsModule, Loader],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './check-in-out.html',
})
export class CheckInOut implements OnInit {
  todayRecord?: AttendanceRecord;
  recentRecords: AttendanceRecord[] = [];
  loading = false;
  error = '';
  success = '';
  notes = '';
  location = '';

  constructor(private attendanceService: AttendanceService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadRecent();
  }

  loadRecent(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.attendanceService.list(0, 10).subscribe({
      next: (res) => {
        this.recentRecords = res.content;
        const today = new Date().toISOString().split('T')[0];
        this.todayRecord = res.content.find((r) => r.attendanceDate === today);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load attendance';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  checkIn(): void {
    this.error = '';
    this.attendanceService
      .checkIn({ notes: this.notes, checkInLocation: this.location })
      .subscribe({
        next: (r) => {
          this.todayRecord = r;
          this.success = 'Checked in successfully';
          this.notes = '';
          this.loadRecent();
        },
        error: (err) => (this.error = err?.error?.message || 'Check-in failed'),
      });
  }

  checkOut(): void {
    if (!this.todayRecord) return;
    this.error = '';
    this.attendanceService
      .checkOut(this.todayRecord.id, { checkOutLocation: this.location })
      .subscribe({
        next: (r) => {
          this.todayRecord = r;
          this.success = 'Checked out successfully';
          this.loadRecent();
        },
        error: (err) => (this.error = err?.error?.message || 'Check-out failed'),
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
}
