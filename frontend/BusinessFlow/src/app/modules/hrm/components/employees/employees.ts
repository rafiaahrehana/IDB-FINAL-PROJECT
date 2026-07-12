import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Employee,
  CreateEmployeeRequest,
  Department,
  Designation,
  EmploymentStatus,
  EMPLOYMENT_STATUSES,
  EMPLOYMENT_TYPES,
} from '../../models/hrm.model';
import { EmployeeService } from '../../services/employee.service';
import { DepartmentService } from '../../services/department.service';
import { DesignationService } from '../../services/designation.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { LocationComponent } from '../../../../shared/components/location/location.component';

@Component({
  selector: 'app-employees',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog, LocationComponent],
  templateUrl: './employees.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './employees.scss',
})
export class Employees implements OnInit {
  employees: Employee[] = [];
  departments: Department[] = [];
  designations: Designation[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  departmentFilter: number | '' = '';
  statusFilter: EmploymentStatus | '' = '';

  statuses = EMPLOYMENT_STATUSES;
  types = EMPLOYMENT_TYPES;

  showForm = false;
  saving = false;
  form: CreateEmployeeRequest = this.emptyForm();

  terminateTarget: Employee | null = null;

  constructor(
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private designationService: DesignationService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
    this.departmentService.listActive().subscribe({ next: (d) => { this.departments = d; this.cdr.markForCheck(); } });
    this.designationService.listActive().subscribe({ next: (d) => { this.designations = d; this.cdr.markForCheck(); } });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.employeeService
      .list(this.page, 20, this.departmentFilter || undefined, this.statusFilter || undefined)
      .subscribe({
        next: (res) => {
          this.employees = res.content;
          this.totalPages = res.totalPages;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.error = 'Failed to load employees';
          this.loading = false;
          this.cdr.markForCheck();
        },
      });
  }

  save(): void {
    this.saving = true;
    this.error = '';
    this.employeeService.create(this.cleanPayload()).subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.form = this.emptyForm();
        this.success = 'Employee created successfully';
        this.page = 0;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to create employee';
        this.cdr.markForCheck();
      },
    });
  }

  confirmTerminate(): void {
    if (!this.terminateTarget) return;
    this.employeeService.terminate(this.terminateTarget.id).subscribe({
      next: () => {
        this.terminateTarget = null;
        this.success = 'Employee terminated';
        this.cdr.markForCheck();
        this.load();
      },
      error: () => {
        this.terminateTarget = null;
        this.error = 'Failed to terminate employee';
        this.cdr.markForCheck();
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  private cleanPayload(): CreateEmployeeRequest {
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null) delete payload[k];
    });
    return payload;
  }

  private emptyForm(): CreateEmployeeRequest {
    return {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      employmentType: 'FULL_TIME',
    };
  }

  onLocationChange(location: any) {
    this.form.location = location;
  }
}
