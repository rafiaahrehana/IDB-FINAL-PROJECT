import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeePortalService, PayrollRecord } from '../../services/employee-portal.service';

@Component({
  selector: 'app-employee-payroll',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <div>
        <h4 class="page-title">My Payroll</h4>
        <p class="page-subtitle">View your salary slips and payment history.</p>
      </div>
    </div>
    @if (loading) {
      <div class="text-center py-5"><div class="spinner-border text-primary"></div></div>
    } @else if (records.length) {
      <div class="row g-4">
        @for (r of records; track r.id) {
          <div class="col-md-6">
            <div class="card border-0 shadow-sm p-4">
              <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <h6 class="fw-bold mb-1">{{ getMonthName(r.payMonth) }} {{ r.payYear }}</h6>
                  <span class="badge" [class]="r.status === 'PAID' ? 'bg-success' : 'bg-warning'">{{ r.status }}</span>
                </div>
                <h5 class="fw-bold mb-0" style="color: #2563EB">{{ r.netSalary | currency }}</h5>
              </div>
              <div class="row g-2">
                <div class="col-6"><small class="text-muted">Basic</small><div class="fw-semibold">{{ r.basicSalary | currency }}</div></div>
                <div class="col-6"><small class="text-muted">House Rent</small><div class="fw-semibold">{{ r.houseRent | currency }}</div></div>
                <div class="col-6"><small class="text-muted">Medical</small><div class="fw-semibold">{{ r.medicalAllowance | currency }}</div></div>
                <div class="col-6"><small class="text-muted">Transport</small><div class="fw-semibold">{{ r.transportAllowance | currency }}</div></div>
              </div>
              @if (r.paidAt) {
                <div class="mt-3 pt-3 border-top"><small class="text-muted">Paid on {{ r.paidAt | date:'mediumDate' }}</small></div>
              }
            </div>
          </div>
        }
      </div>
    } @else {
      <div class="text-center py-5 text-muted">
        <i class="bi bi-cash-stack" style="font-size: 3rem; opacity: 0.3"></i>
        <p class="mt-3">No payroll records found.</p>
      </div>
    }
  `,
  styles: [".page-header { margin-bottom: 1.5rem; } .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; } .page-subtitle { color: #64748b; margin: 0; }"]
})
export class EmployeePayrollComponent implements OnInit {
  private empService = inject(EmployeePortalService);
  records: PayrollRecord[] = [];
  loading = true;

  ngOnInit(): void {
    this.empService.getMyPayroll().subscribe({
      next: (res: any) => { this.records = res.content || res || []; this.loading = false; },
      error: () => this.loading = false
    });
  }

  getMonthName(m: number): string {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[m - 1] || '';
  }
}
