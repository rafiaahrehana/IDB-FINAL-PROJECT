import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Timesheet, TimesheetRequest } from '../../models/attendance.model';
import { TimesheetService } from '../../services/timesheet.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-timesheets',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './timesheets.html',
})
export class Timesheets implements OnInit {
  // MY TIMESHEETS
  myTimesheets: Timesheet[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: TimesheetRequest = this.emptyForm();
  deleteTarget: Timesheet | null = null;

  // MANAGER VIEW - look up a specific employee's timesheets by ID
  employeeIdFilter?: number;
  employeeTimesheets: Timesheet[] = [];
  employeePage = 0;
  employeeTotalPages = 0;
  employeeLoading = false;

  constructor(private timesheetService: TimesheetService) {}

  ngOnInit(): void {
    this.load();
  }

  emptyForm(): TimesheetRequest {
    return { workDate: new Date().toISOString().slice(0, 10), hoursWorked: 0, billableHours: 0, projectName: '', taskDescription: '' };
  }

  load(): void {
    this.loading = true;
    this.timesheetService.listMine(this.page).subscribe({
      next: (res) => {
        this.myTimesheets = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load timesheets';
        this.loading = false;
      },
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  openEdit(t: Timesheet): void {
    this.editingId = t.id;
    this.form = {
      workDate: t.workDate,
      startTime: t.startTime,
      endTime: t.endTime,
      hoursWorked: t.hoursWorked,
      billableHours: t.billableHours,
      projectName: t.projectName,
      taskDescription: t.taskDescription,
      description: t.description,
    };
    this.showForm = true;
  }

  save(): void {
    const op = this.editingId
      ? this.timesheetService.update(this.editingId, this.form)
      : this.timesheetService.log(this.form);
    op.subscribe({
      next: () => {
        this.showForm = false;
        this.success = this.editingId ? 'Timesheet updated' : 'Hours logged';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to save timesheet'),
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.timesheetService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete an approved timesheet';
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  // MANAGER VIEW
  loadEmployeeTimesheets(): void {
    if (!this.employeeIdFilter) return;
    this.employeeLoading = true;
    this.timesheetService.listForEmployee(this.employeeIdFilter, this.employeePage).subscribe({
      next: (res) => {
        this.employeeTimesheets = res.content;
        this.employeeTotalPages = res.totalPages;
        this.employeeLoading = false;
      },
      error: () => {
        this.error = 'Failed to load timesheets for that employee';
        this.employeeLoading = false;
      },
    });
  }

  approve(t: Timesheet): void {
    this.timesheetService.approve(t.id).subscribe({
      next: () => {
        this.success = 'Timesheet approved';
        this.loadEmployeeTimesheets();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to approve'),
    });
  }

  goToEmployeePage(p: number): void {
    this.employeePage = p;
    this.loadEmployeeTimesheets();
  }
}
