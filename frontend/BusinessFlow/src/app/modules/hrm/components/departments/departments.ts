import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Department, DepartmentRequest, Employee } from '../../models/hrm.model';
import { DepartmentService } from '../../services/department.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-departments',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog, HasPermissionDirective],
  templateUrl: './departments.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './departments.scss',
})
export class Departments implements OnInit {
  departments: Department[] = [];
  activeDepartments: Department[] = [];
  employees: Employee[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: DepartmentRequest = { name: '' };

  deleteTarget: Department | null = null;

  constructor(
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
    this.employeeService.list(0, 1000, undefined, 'ACTIVE').subscribe({ next: (res) => { this.employees = res.content; this.cdr.markForCheck(); } });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.departmentService.list(this.page, 20).subscribe({
      next: (res) => {
        this.departments = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load departments';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
    this.departmentService.listActive().subscribe({ next: (d) => { this.activeDepartments = d; this.cdr.markForCheck(); } });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = { name: '' };
    this.showForm = true;
  }

  openEdit(dept: Department): void {
    this.editingId = dept.id;
    this.form = {
      name: dept.name,
      code: dept.code,
      description: dept.description,
      headEmployeeId: dept.headEmployeeId,
      parentDepartmentId: dept.parentDepartmentId,
      budget: dept.budget,
    };
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null) delete payload[k];
    });
    const req = this.editingId
      ? this.departmentService.update(this.editingId, payload)
      : this.departmentService.create(payload);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Department updated' : 'Department created';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save department';
        this.cdr.markForCheck();
      },
    });
  }

  toggle(dept: Department): void {
    this.departmentService.toggle(dept.id).subscribe({
      next: () => this.load(),
      error: () => { this.error = 'Failed to update department status'; this.cdr.markForCheck(); },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.departmentService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Department deleted';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete department';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
