import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
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
import { CustomRoleService } from '../../../platform-admin/services/custom-role.service';
import { CustomRole } from '../../../platform-admin/models/platform-admin.model';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-employees',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './employees.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './employees.scss',
})
export class Employees implements OnInit {
  employees: Employee[] = [];
  departments: Department[] = [];
  designations: Designation[] = [];
  customRoles: CustomRole[] = [];
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
    private customRoleService: CustomRoleService,
  ) {}

  ngOnInit(): void {
    this.load();
    this.departmentService.listActive().subscribe({ next: (d) => (this.departments = d) });
    this.designationService.listActive().subscribe({ next: (d) => (this.designations = d) });
    this.customRoleService.list().subscribe({ next: (r) => (this.customRoles = r) });
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
        },
        error: () => {
          this.error = 'Failed to load employees';
          this.loading = false;
        },
      });
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const payload = this.cleanPayload();
    const roleId = payload.customRoleId;
    delete payload.customRoleId;
    this.employeeService.create(payload).subscribe({
      next: (emp) => {
        if (roleId && emp.userId) {
          this.customRoleService.assignToUser(roleId, emp.userId).subscribe({
            next: () => this.onCreated(),
            error: () => this.onCreated(),
          });
        } else {
          this.onCreated();
        }
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to create employee';
      },
    });
  }

  private onCreated(): void {
    this.saving = false;
    this.showForm = false;
    this.form = this.emptyForm();
    this.success = 'Employee created successfully';
    this.page = 0;
    this.load();
  }

  confirmTerminate(): void {
    if (!this.terminateTarget) return;
    this.employeeService.terminate(this.terminateTarget.id).subscribe({
      next: () => {
        this.terminateTarget = null;
        this.success = 'Employee terminated';
        this.load();
      },
      error: () => {
        this.terminateTarget = null;
        this.error = 'Failed to terminate employee';
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
}
