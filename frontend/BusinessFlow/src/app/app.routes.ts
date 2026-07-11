import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { DashboardComponent } from './modules/dashboard/components/dashboard.component';
import { Login } from './modules/auth/login/login';
import { Register } from './modules/auth/register/register';
import { ForgotPassword } from './modules/auth/forgot-password/forgot-password';
import { ResetPassword } from './modules/auth/reset-password/reset-password';
import { VerifyEmail } from './modules/auth/verify-email/verify-email';
import { NotFound } from './shared/components/not-found/not-found';
import { Forbidden } from './shared/components/forbidden/forbidden';
import { ServerError } from './shared/components/server-error/server-error';
import { GlobalSearch } from './modules/search/components/global-search/global-search';
import { AiAssistant } from './modules/ai/components/ai-assistant/ai-assistant';
import { Notifications } from './modules/notifications/notifications';
import { Preferences as NotificationPreferences } from './modules/preferences/preferences';

// Portal (public landing + per-company public portal + owner settings)
import { Landing } from './modules/landing/landing';
import { CompanyPortal } from './modules/portal/company-portal/company-portal';
import { CompanySettings } from './modules/portal/company-settings/company-settings';
import { PaymentResult } from './modules/portal/payment-result/payment-result';

// Finance
import { ChartOfAccounts } from './modules/finance/components/chart-of-accounts/chart-of-accounts';
import { Expenses } from './modules/finance/components/expenses/expenses';
import { Invoices } from './modules/finance/components/invoices/invoices';
import { Reports as FinanceReports } from './modules/finance/components/reports/reports';
import { WalletPage } from './modules/finance/components/wallet/wallet';
import { PaymentReceipts } from './modules/finance/components/payment-receipts/payment-receipts';
import { GeneralLedger } from './modules/finance/components/general-ledger/general-ledger';
import { BankReconciliationPage } from './modules/finance/components/bank-reconciliation/bank-reconciliation';
import { JournalEntries } from './modules/finance/components/journal-entries/journal-entries';

// Support
import { Tickets } from './modules/support/components/tickets/tickets';
import { Agents } from './modules/support/components/agents/agents';
import { Categories } from './modules/support/components/categories/categories';
import { Messages } from './modules/support/components/messages/messages';
import { SlaPolicies } from './modules/support/components/sla-policies/sla-policies';
import { AuditLogs } from './modules/support/components/audit-logs/audit-logs';
import { ContextSwitches } from './modules/support/components/context-switches/context-switches';

// ITAM
import { Hardware } from './modules/itam/components/hardware/hardware';
import { Software } from './modules/itam/components/software/software';
import { Assignments } from './modules/itam/components/assignments/assignments';
import { Offboarding } from './modules/itam/components/offboarding/offboarding';
import { AssetImport } from './modules/itam/components/asset-import/asset-import';

// Attendance
import { CheckInOut } from './modules/attendance/components/check-in-out/check-in-out';
import { AttendanceList } from './modules/attendance/components/attendance-list/attendance-list';
import { LeaveManagement } from './modules/attendance/components/leave-management/leave-management';
import { Reports as AttendanceReports } from './modules/attendance/components/reports/reports';
import { BiometricDataPage } from './modules/attendance/components/biometric-data/biometric-data';
import { ShiftAssignments } from './modules/attendance/components/shift-assignments/shift-assignments';
import { Timesheets } from './modules/attendance/components/timesheets/timesheets';

export const routes: Routes = [
  { path: '', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  // Public pages - no auth
  { path: 'home', component: Landing },
  { path: 'portal/:subdomain', loadChildren: () => import('./modules/site/site.routes').then(m => m.SITE_ROUTES), canActivate: [AuthGuard] },
  { path: 'payment-result', component: PaymentResult },
  // Owner-editable portal settings

  {
    path: 'portal-settings',
    component: CompanySettings,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['COMPANY_OWNER'] },
  },
  { path: 'auth/login', component: Login },
  { path: 'auth/register', component: Register },
  { path: 'auth/forgot-password', component: ForgotPassword },
  { path: 'auth/reset-password', component: ResetPassword },
  { path: 'auth/verify-email', component: VerifyEmail },
  { path: 'auth', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'search', component: GlobalSearch, canActivate: [AuthGuard] },
  { path: 'ai', component: AiAssistant, canActivate: [AuthGuard] },
  { path: 'notifications', component: Notifications, canActivate: [AuthGuard] },
  { path: 'notifications/preferences', component: NotificationPreferences, canActivate: [AuthGuard] },
  {
    path: 'crm',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/crm/crm.routes').then(m => m.CRM_ROUTES)
  },
  {
    path: 'servicedesk',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/servicedesk/servicedesk.routes').then(m => m.SERVICEDESK_ROUTES)
  },
  {
    path: 'hrm',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/hrm/hrm.routes').then(m => m.HRM_ROUTES)
  },
  {
    path: 'platform',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'] },
    loadChildren: () => import('./modules/platform-admin/platform-admin.routes').then(m => m.PLATFORM_ADMIN_ROUTES)
  },
  {
    path: 'finance',
    canActivate: [AuthGuard, RoleGuard],
    children: [
      { path: 'coa', component: ChartOfAccounts },
      { path: 'expenses', component: Expenses },
      { path: 'invoices', component: Invoices },
      { path: 'journal-entries', component: JournalEntries },
      { path: 'reports', component: FinanceReports },
      { path: 'wallet', component: WalletPage },
      { path: 'payment-receipts', component: PaymentReceipts },
      { path: 'general-ledger', component: GeneralLedger },
      { path: 'bank-reconciliation', component: BankReconciliationPage },
      { path: '', redirectTo: 'invoices', pathMatch: 'full' }
    ]
  },
  {
    path: 'support',
    canActivate: [AuthGuard],
    children: [
      { path: 'tickets', component: Tickets },
      { path: 'agents', component: Agents },
      { path: 'categories', component: Categories },
      { path: 'messages', component: Messages },
      { path: 'sla-policies', component: SlaPolicies },
      { path: 'audit-logs', component: AuditLogs },
      { path: 'context-switches', component: ContextSwitches },
      { path: '', redirectTo: 'tickets', pathMatch: 'full' }
    ]
  },
  {
    path: 'itam',
    canActivate: [AuthGuard, RoleGuard],
    children: [
      { path: 'hardware', component: Hardware },
      { path: 'software', component: Software },
      { path: 'assignments', component: Assignments },
      { path: 'offboarding', component: Offboarding },
      { path: 'import', component: AssetImport },
      { path: '', redirectTo: 'hardware', pathMatch: 'full' }
    ]
  },
  {
    path: 'attendance',
    canActivate: [AuthGuard],
    children: [
      { path: 'check-in', component: CheckInOut },
      { path: 'records', component: AttendanceList },
      { path: 'leaves', component: LeaveManagement },
      { path: 'timesheets', component: Timesheets },
      { path: 'shift-assignments', component: ShiftAssignments },
      { path: 'biometric-data', component: BiometricDataPage },
      { path: 'reports', component: AttendanceReports },
      { path: '', redirectTo: 'check-in', pathMatch: 'full' }
    ]
  },
  { path: 'forbidden', component: Forbidden },
  { path: 'server-error', component: ServerError },
  { path: '**', component: NotFound }
];
