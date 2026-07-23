import { Routes } from '@angular/router';
import { Employees } from './components/employees/employees';
import { EmployeeDetail } from './components/employee-detail/employee-detail';
import { Departments } from './components/departments/departments';
import { Designations } from './components/designations/designations';
import { PayrollPage } from './components/payroll/payroll';
import { SalaryStructures } from './components/salary-structures/salary-structures';
import { Announcements } from './components/announcements/announcements';
import { Holidays } from './components/holidays/holidays';
import { LeavePolicies } from './components/leave-policies/leave-policies';
import { Shifts } from './components/shifts/shifts';
import { PerformanceReviews } from './components/performance-reviews/performance-reviews';
import { JobPostings } from './components/job-postings/job-postings';
import { OfferLetters } from './components/offer-letters/offer-letters';
import { Leaves } from './components/leaves/leaves';
import { Applications } from './components/applications/applications';
import { Assets } from './components/assets/assets';
import { Expenses } from './components/expenses/expenses';

export const HRM_ROUTES: Routes = [
  { path: 'employees', component: Employees, data: { requiredPermission: 'EMPLOYEE_VIEW' } },
  { path: 'employees/:id', component: EmployeeDetail, data: { requiredPermission: 'EMPLOYEE_VIEW' } },
  { path: 'departments', component: Departments, data: { requiredPermission: 'DEPARTMENT_VIEW' } },
  { path: 'designations', component: Designations, data: { requiredPermission: 'DESIGNATION_VIEW' } },
  { path: 'payroll', component: PayrollPage, data: { requiredPermission: 'PAYROLL_VIEW' } },
  { path: 'leaves', component: Leaves, data: { requiredPermission: 'LEAVE_VIEW' } },
  { path: 'expenses', component: Expenses, data: { requiredPermission: 'EXPENSE_VIEW' } },
  { path: 'salary-structures', component: SalaryStructures, data: { requiredPermission: 'SALARY_STRUCTURE_VIEW' } },
  { path: 'announcements', component: Announcements, data: { requiredPermission: 'ANNOUNCEMENT_VIEW' } },
  { path: 'holidays', component: Holidays, data: { requiredPermission: 'HOLIDAY_VIEW' } },
  { path: 'leave-policies', component: LeavePolicies, data: { requiredPermission: 'LEAVE_POLICY_VIEW' } },
  { path: 'shifts', component: Shifts, data: { requiredPermission: 'SHIFT_VIEW' } },
  { path: 'performance', component: PerformanceReviews, data: { requiredPermission: 'PERFORMANCE_VIEW' } },
  { path: 'job-postings', component: JobPostings, data: { requiredPermission: 'JOB_POSTING_VIEW' } },
  { path: 'letters', component: OfferLetters, data: { requiredPermission: 'LETTER_VIEW' } },
  { path: 'applications', component: Applications, data: { requiredPermission: 'APPLICATION_VIEW' } },
  { path: 'assets', component: Assets, data: { requiredPermission: 'ASSET_VIEW' } },
  { path: '', redirectTo: 'employees', pathMatch: 'full' }
];
