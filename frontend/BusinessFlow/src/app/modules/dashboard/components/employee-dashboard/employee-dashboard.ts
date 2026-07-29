import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { EmployeeService } from '../../../hrm/services/employee.service';
import { AnnouncementService } from '../../../hrm/services/announcement.service';
import { HolidayService } from '../../../hrm/services/holiday.service';
import { AttendanceService } from '../../../attendance/services/attendance.service';
import { Employee } from '../../../hrm/models/hrm.model';
import { AttendanceRecord, MyAttendanceMonthlySummary } from '../../../attendance/models/attendance.model';
import { Loader } from '../../../../shared/components/loader/loader';

interface NoticeItem {
  title: string;
  date: string;
  kind: 'Announcement' | 'Holiday';
}

@Component({
  selector: 'app-employee-dashboard',
  imports: [CommonModule, RouterLink, Loader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './employee-dashboard.html',
})
export class EmployeeDashboard implements OnInit {
  profile?: Employee;
  todayRecord?: AttendanceRecord;
  monthlySummary?: MyAttendanceMonthlySummary;
  notices: NoticeItem[] = [];

  loadingProfile = false;
  loadingAttendance = false;
  checkingInOut = false;
  error = '';
  success = '';

  constructor(
    public auth: AuthService,
    public permissionService: PermissionService,
    private employeeService: EmployeeService,
    private announcementService: AnnouncementService,
    private holidayService: HolidayService,
    private attendanceService: AttendanceService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadToday();
    this.loadMonthlySummary();
    this.loadNoticeBoard();
  }

  get roleLabel(): string {
    return this.profile?.customRoleName || 'Employee';
  }

  loadProfile(): void {
    this.loadingProfile = true;
    this.employeeService.getMyProfile().subscribe({
      next: (p) => { this.profile = p; this.loadingProfile = false; this.cdr.markForCheck(); },
      error: () => { this.loadingProfile = false; this.cdr.markForCheck(); },
    });
  }

  loadToday(): void {
    this.attendanceService.myToday().subscribe({
      next: (rec) => { this.todayRecord = rec || undefined; this.cdr.markForCheck(); },
      error: () => { this.todayRecord = undefined; this.cdr.markForCheck(); },
    });
  }

  loadMonthlySummary(): void {
    this.loadingAttendance = true;
    this.attendanceService.myMonthlySummary().subscribe({
      next: (s) => { this.monthlySummary = s; this.loadingAttendance = false; this.cdr.markForCheck(); },
      error: () => { this.loadingAttendance = false; this.cdr.markForCheck(); },
    });
  }

  loadNoticeBoard(): void {
    const today = new Date().toISOString().slice(0, 10);
    this.announcementService.listActive().subscribe({
      next: (list) => {
        const items: NoticeItem[] = (list || []).map(a => ({
          title: a.title, date: a.publishedAt || a.createdAt, kind: 'Announcement' as const,
        }));
        this.notices = [...this.notices, ...items].sort((a, b) => (a.date < b.date ? 1 : -1)).slice(0, 6);
        this.cdr.markForCheck();
      },
      error: () => { /* no ANNOUNCEMENT_VIEW or none active - notice board just shows holidays */ },
    });
    this.holidayService.listCurrentYear().subscribe({
      next: (list) => {
        const upcoming: NoticeItem[] = (list || [])
          .filter(h => h.holidayDate >= today)
          .map(h => ({ title: h.name, date: h.holidayDate, kind: 'Holiday' as const }));
        this.notices = [...this.notices, ...upcoming].sort((a, b) => (a.date < b.date ? 1 : -1)).slice(0, 6);
        this.cdr.markForCheck();
      },
      error: () => {},
    });
  }

  checkIn(): void {
    this.checkingInOut = true;
    this.error = '';
    const timeStr = new Date().toTimeString().split(' ')[0];
    this.attendanceService.checkIn({ checkInTime: timeStr, method: 'MANUAL' }).subscribe({
      next: (r) => {
        this.todayRecord = r;
        this.checkingInOut = false;
        this.success = 'Checked in successfully';
        this.cdr.markForCheck();
        this.loadMonthlySummary();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Check-in failed';
        this.checkingInOut = false;
        this.cdr.markForCheck();
      },
    });
  }

  checkOut(): void {
    if (!this.todayRecord) return;
    this.checkingInOut = true;
    this.error = '';
    const timeStr = new Date().toTimeString().split(' ')[0];
    this.attendanceService.checkOut(this.todayRecord.id, { checkOutTime: timeStr, method: 'MANUAL' }).subscribe({
      next: (r) => {
        this.todayRecord = r;
        this.checkingInOut = false;
        this.success = 'Checked out successfully';
        this.cdr.markForCheck();
        this.loadMonthlySummary();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Check-out failed';
        this.checkingInOut = false;
        this.cdr.markForCheck();
      },
    });
  }
}
