import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Payroll } from '../../models/hrm.model';
import { PayrollService } from '../../services/payroll.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-my-payslips',
  imports: [CommonModule, Pagination, Loader, EmptyState],
  templateUrl: './my-payslips.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyPayslips implements OnInit {
  payslips: Payroll[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';

  private employeeId: number | null = null;

  constructor(
    private payrollService: PayrollService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.employeeService.getMyProfile().subscribe({
      next: (emp) => {
        this.employeeId = emp.id;
        this.load();
      },
      error: () => {
        this.error = 'Failed to load your profile';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  load(): void {
    if (!this.employeeId) return;
    this.loading = true;
    this.error = '';
    this.payrollService.listForEmployee(this.employeeId, this.page).subscribe({
      next: (res) => {
        this.payslips = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load payslips';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  statusClass(status: string): string {
    return (
      {
        DRAFT: 'text-bg-secondary',
        APPROVED: 'text-bg-info',
        PAID: 'text-bg-success',
        CANCELLED: 'text-bg-danger',
      }[status] || 'text-bg-light'
    );
  }
}
