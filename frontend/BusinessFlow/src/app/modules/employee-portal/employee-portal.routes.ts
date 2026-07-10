import { Routes } from '@angular/router';

export const EMPLOYEE_PORTAL_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./pages/dashboard/employee-dashboard.component').then(m => m.EmployeeDashboardComponent) },
  { path: 'profile', loadComponent: () => import('./pages/profile/employee-profile.component').then(m => m.EmployeeProfileComponent) },
  { path: 'leaves', loadComponent: () => import('./pages/leaves/employee-leaves.component').then(m => m.EmployeeLeavesComponent) },
  { path: 'tasks', loadComponent: () => import('./pages/tasks/employee-tasks.component').then(m => m.EmployeeTasksComponent) },
  { path: 'payroll', loadComponent: () => import('./pages/payroll/employee-payroll.component').then(m => m.EmployeePayrollComponent) },
];
