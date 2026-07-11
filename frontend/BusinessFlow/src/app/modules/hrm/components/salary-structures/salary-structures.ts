import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SalaryStructure, SalaryStructureRequest, Employee } from '../../models/hrm.model';
import { SalaryStructureService } from '../../services/salary-structure.service';
import { EmployeeService } from '../../services/employee.service';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-salary-structures',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog],
  templateUrl: './salary-structures.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './salary-structures.scss',
})
export class SalaryStructures implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  structures: SalaryStructure[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  form: SalaryStructureRequest = this.emptyForm();

  deleteTarget: SalaryStructure | null = null;

  constructor(
    private salaryService: SalaryStructureService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeService.list(0, 100).subscribe({ next: (res) => (this.employees = res.content) });
  }

  load(): void {
    if (!this.selectedEmployeeId) {
      this.structures = [];
      return;
    }
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.salaryService.history(this.selectedEmployeeId).subscribe({
      next: (res) => {
        this.structures = res;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load salary structures';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    if (!this.selectedEmployeeId) return;
    this.form = this.emptyForm();
    this.form.employeeId = this.selectedEmployeeId;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.cdr.markForCheck();
    this.error = '';
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null || payload[k] === undefined) delete payload[k];
    });
    this.salaryService.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.cdr.markForCheck();
        this.showForm = false;
        this.success = 'Salary structure created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.cdr.markForCheck();
        this.error = err?.error?.message || 'Failed to create salary structure';
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.salaryService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Salary structure deleted';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete';
        this.deleteTarget = null;
      },
    });
  }

  isCurrent(s: SalaryStructure): boolean {
    return !s.effectiveTo;
  }

  private emptyForm(): SalaryStructureRequest {
    return {
      employeeId: undefined as any,
      effectiveFrom: '',
      grossSalary: undefined as any,
      basicSalary: undefined as any,
    };
  }
}
