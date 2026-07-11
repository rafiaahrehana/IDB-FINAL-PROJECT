import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeShiftAssignment, EmployeeShiftAssignmentRequest, Shift } from '../../../hrm/models/hrm.model';
import { ShiftAssignmentService } from '../../services/shift-assignment.service';
import { ShiftService } from '../../../hrm/services/shift.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-shift-assignments',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './shift-assignments.html',
})
export class ShiftAssignments implements OnInit {
  assignments: EmployeeShiftAssignment[] = [];
  shifts: Shift[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: EmployeeShiftAssignmentRequest = this.emptyForm();
  deleteTarget: EmployeeShiftAssignment | null = null;

  employeeIdLookup?: number;
  lookedUpAssignment: EmployeeShiftAssignment | null = null;

  constructor(private assignmentService: ShiftAssignmentService, private shiftService: ShiftService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.shiftService.listActive().subscribe({ next: (res) => (this.shifts = res) });
    this.load();
  }

  emptyForm(): EmployeeShiftAssignmentRequest {
    return { employeeId: 0, shiftId: 0, assignmentStartDate: new Date().toISOString().slice(0, 10) };
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.assignmentService.list(this.page).subscribe({
      next: (res) => {
        this.assignments = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load shift assignments';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  openEdit(a: EmployeeShiftAssignment): void {
    this.editingId = a.id;
    this.form = {
      employeeId: a.employeeId,
      shiftId: a.shiftId,
      assignmentStartDate: a.assignmentStartDate,
      assignmentEndDate: a.assignmentEndDate,
      reason: a.reason,
      notes: a.notes,
    };
    this.showForm = true;
  }

  save(): void {
    if (!this.form.employeeId || !this.form.shiftId) return;
    const op = this.editingId
      ? this.assignmentService.update(this.editingId, this.form)
      : this.assignmentService.create(this.form);
    op.subscribe({
      next: () => {
        this.showForm = false;
        this.success = this.editingId ? 'Assignment updated' : 'Shift assigned';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to save assignment'),
    });
  }

  endAssignment(a: EmployeeShiftAssignment): void {
    this.assignmentService.endAssignment(a.id).subscribe({
      next: () => {
        this.success = 'Assignment ended';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to end assignment'),
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.assignmentService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete';
      },
    });
  }

  lookupByEmployee(): void {
    if (!this.employeeIdLookup) return;
    this.assignmentService.getByEmployee(this.employeeIdLookup).subscribe({
      next: (res) => (this.lookedUpAssignment = res),
      error: () => {
        this.lookedUpAssignment = null;
        this.error = 'No active shift assignment for that employee';
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
