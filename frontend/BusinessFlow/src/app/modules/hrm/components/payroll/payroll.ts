import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Payroll, CreatePayrollRequest, Employee } from '../../models/hrm.model';
import { PayrollService } from '../../services/payroll.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-payroll',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './payroll.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './payroll.scss',
})
export class PayrollPage implements OnInit {
  payrolls: Payroll[] = [];
  employees: Employee[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  years: number[] = [];
  month: number;
  year: number;

  showForm = false;
  form: CreatePayrollRequest = this.emptyForm();

  payTarget: Payroll | null = null;
  paymentReference = '';
  deleteTarget: Payroll | null = null;

  constructor(
    private payrollService: PayrollService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {
    const now = new Date();
    this.month = now.getMonth() + 1;
    this.year = now.getFullYear();
    for (let y = this.year + 1; y >= 2020; y--) this.years.push(y);
  }

  ngOnInit(): void {
    this.load();
    this.employeeService.list(0, 100).subscribe({ next: (res) => (this.employees = res.content) });
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.payrollService.listByPeriod(this.month, this.year, this.page, 50).subscribe({
      next: (res) => {
        this.payrolls = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load payroll';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  onEmployeeSelected(): void {
    const emp = this.employees.find((e) => e.id === this.form.employeeId);
    if (emp) {
      this.form.basicSalary = emp.basicSalary ?? this.form.basicSalary;
      this.form.houseRent = emp.houseRent;
      this.form.medicalAllowance = emp.medicalAllowance;
      this.form.transportAllowance = emp.transportAllowance;
    }
  }

  save(): void {
    this.saving = true;
    this.cdr.markForCheck();
    this.error = '';
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null || payload[k] === undefined) delete payload[k];
    });
    this.payrollService.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.cdr.markForCheck();
        this.showForm = false;
        this.form = this.emptyForm();
        this.success = 'Payroll created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.cdr.markForCheck();
        this.error = err?.error?.message || 'Failed to create payroll';
      },
    });
  }

  approve(p: Payroll): void {
    this.payrollService.approve(p.id).subscribe({
      next: () => {
        this.success = 'Payroll approved';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to approve payroll'),
    });
  }

  openPay(p: Payroll): void {
    this.payTarget = p;
    this.paymentReference = '';
  }

  confirmPay(): void {
    if (!this.payTarget) return;
    this.payrollService
      .markPaid(this.payTarget.id, this.paymentReference.trim() || undefined)
      .subscribe({
        next: () => {
          this.payTarget = null;
          this.success = 'Payroll marked as paid';
          this.load();
        },
        error: (err) => {
          this.payTarget = null;
          this.error = err?.error?.message || 'Failed to mark as paid';
        },
      });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.payrollService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Payroll deleted';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete payroll';
        this.deleteTarget = null;
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  private emptyForm(): CreatePayrollRequest {
    return {
      employeeId: undefined as any,
      payMonth: this.month ?? new Date().getMonth() + 1,
      payYear: this.year ?? new Date().getFullYear(),
      basicSalary: undefined as any,
    };
  }
}
