import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Department, DepartmentRequest } from '../../models/hrm.model';
import { Employee } from '../../models/hrm.model';
import { DepartmentService } from '../../services/department.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-departments',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './departments.html',
  changeDetection: ChangeDetectionStrategy.Eager,
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
  searchTerm = '';

  deleteTarget: Department | null = null;

  // Department employees modal
  showEmployees = false;
  deptEmployees: Employee[] = [];
  deptEmployeesLoading = false;
  selectedDeptName = '';

  constructor(
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
  ) {}

  ngOnInit(): void {
    this.load();
    this.employeeService.list(0, 200).subscribe({
      next: (res) => (this.employees = res.content),
    });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.departmentService.list(this.page, 50).subscribe({
      next: (res) => {
        this.departments = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load departments';
        this.loading = false;
      },
    });
    this.departmentService.listActive().subscribe({ next: (d) => (this.activeDepartments = d) });
  }

  get filteredDepartments(): Department[] {
    if (!this.searchTerm.trim()) return this.departments;
    const term = this.searchTerm.toLowerCase();
    return this.departments.filter(d =>
      d.name.toLowerCase().includes(term) ||
      (d.code && d.code.toLowerCase().includes(term)) ||
      (d.description && d.description.toLowerCase().includes(term))
    );
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
      if (payload[k] === '' || payload[k] === null || payload[k] === undefined) delete payload[k];
    });
    const req = this.editingId
      ? this.departmentService.update(this.editingId, payload)
      : this.departmentService.create(payload);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Department updated' : 'Department created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save department';
      },
    });
  }

  toggle(dept: Department): void {
    this.departmentService.toggle(dept.id).subscribe({
      next: () => this.load(),
      error: () => (this.error = 'Failed to update department status'),
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.departmentService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Department deleted';
        this.load();
      },
      error: (err) => {
        this.deleteTarget = null;
        this.error = err?.error?.message || 'Failed to delete department';
      },
    });
  }

  viewEmployees(dept: Department): void {
    if (dept.employeeCount === 0) return;
    this.selectedDeptName = dept.name;
    this.showEmployees = true;
    this.deptEmployeesLoading = true;
    this.employeeService.list(0, 100, dept.id).subscribe({
      next: (res) => {
        this.deptEmployees = res.content;
        this.deptEmployeesLoading = false;
      },
      error: () => {
        this.deptEmployees = [];
        this.deptEmployeesLoading = false;
      },
    });
  }

  getEmployeeName(emp: Employee): string {
    return `${emp.firstName} ${emp.lastName}`;
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
