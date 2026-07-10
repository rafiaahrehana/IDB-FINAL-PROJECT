import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { DashboardComponent } from './modules/dashboard/components/dashboard.component';
import { LandingPage } from './modules/landing/landing.component';
import { guestGuard } from './modules/landing/guest.guard';
import { Login } from './modules/auth/login/login';
import { Register } from './modules/auth/register/register';
import { ClientRegister } from './modules/auth/client-register/client-register';
import { NotFound } from './shared/components/not-found/not-found';
import { GlobalSearch } from './modules/search/components/global-search/global-search';
import { AiAssistant } from './modules/ai/components/ai-assistant/ai-assistant';
import { AiSettings } from './modules/ai/components/ai-settings/ai-settings';
import { Notifications } from './modules/notifications/notifications';
import { Preferences as NotificationPreferences } from './modules/preferences/preferences';

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
import { SslCommerzCheckout } from './modules/finance/components/sslcommerz-checkout/sslcommerz-checkout';
import { SslCommerzSuccess } from './modules/finance/components/sslcommerz-success/sslcommerz-success';
import { SslCommerzFail } from './modules/finance/components/sslcommerz-fail/sslcommerz-fail';
import { SslCommerzCancel } from './modules/finance/components/sslcommerz-cancel/sslcommerz-cancel';

// Subscription
import { SubscriptionPage } from './modules/company/subscription/subscription';
import { SubscriptionSuccess } from './modules/company/subscription/subscription-success';
import { SubscriptionFail } from './modules/company/subscription/subscription-fail';
import { SubscriptionCancel } from './modules/company/subscription/subscription-cancel';

// Company Settings
import { CompanySettings } from './modules/company/settings/company-settings';

// Support
import { Tickets } from './modules/support/components/tickets/tickets';
import { Agents } from './modules/support/components/agents/agents';
import { Categories } from './modules/support/components/categories/categories';
import { Messages } from './modules/support/components/messages/messages';
import { SlaPolicies } from './modules/support/components/sla-policies/sla-policies';
import { AuditLogs } from './modules/support/components/audit-logs/audit-logs';
import { ContextSwitches } from './modules/support/components/context-switches/context-switches';
import { ClientPortal } from './modules/support/components/client-portal/client-portal';

// ITAM
import { Hardware } from './modules/itam/components/hardware/hardware';
import { Software } from './modules/itam/components/software/software';
import { Assignments } from './modules/itam/components/assignments/assignments';
import { Offboarding } from './modules/itam/components/offboarding/offboarding';

// Attendance
import { CheckInOut } from './modules/attendance/components/check-in-out/check-in-out';
import { AttendanceList } from './modules/attendance/components/attendance-list/attendance-list';
import { LeaveManagement } from './modules/attendance/components/leave-management/leave-management';
import { Reports as AttendanceReports } from './modules/attendance/components/reports/reports';
import { BiometricDataPage } from './modules/attendance/components/biometric-data/biometric-data';
import { ShiftAssignments } from './modules/attendance/components/shift-assignments/shift-assignments';
import { Timesheets } from './modules/attendance/components/timesheets/timesheets';

// Projects / Tasks / Meetings
import { PROJECT_ROUTES } from './modules/project/project.routes';
import { TASK_ROUTES } from './modules/task/task.routes';
import { MEETING_ROUTES } from './modules/meeting/meeting.routes';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: LandingPage, canActivate: [guestGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  {
    path: 'dashboard/employee',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/employee-portal/employee-portal.routes').then(m => m.EMPLOYEE_PORTAL_ROUTES)
  },
  { path: 'auth/login', component: Login },
  { path: 'auth/register', component: Register },
  { path: 'auth/client-register', component: ClientRegister },
  { path: 'auth', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'search', component: GlobalSearch, canActivate: [AuthGuard] },
  { path: 'ai', component: AiAssistant, canActivate: [AuthGuard] },
  { path: 'ai/settings', component: AiSettings, canActivate: [AuthGuard] },
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
      // 'vendors' and 'vendor-payments' routes/pages/services were removed entirely
      // (not just unrouted): there is no separate Vendor entity in the backend - vendor
      // identity is a free-text 'vendorName' field on Expense. Vendor payments are now
      // handled entirely through the Expenses page (submit with vendorName -> approve/
      // reject -> mark as paid). See ExpenseService.getByVendor for filtering by vendor.
      { path: 'journal-entries', component: JournalEntries },
      { path: 'reports', component: FinanceReports },
      { path: 'wallet', component: WalletPage },
      { path: 'payment-receipts', component: PaymentReceipts },
      { path: 'general-ledger', component: GeneralLedger },
      { path: 'bank-reconciliation', component: BankReconciliationPage },
      { path: 'sslcommerz/checkout', component: SslCommerzCheckout, canActivate: [AuthGuard] },
      { path: 'sslcommerz/success', component: SslCommerzSuccess, canActivate: [AuthGuard] },
      { path: 'sslcommerz/fail', component: SslCommerzFail, canActivate: [AuthGuard] },
      { path: 'sslcommerz/cancel', component: SslCommerzCancel, canActivate: [AuthGuard] },
      { path: '', redirectTo: 'invoices', pathMatch: 'full' }
    ]
  },
  {
    path: 'support',
    canActivate: [AuthGuard],
    children: [
      { path: 'tickets', component: Tickets },
      { path: 'client-portal', component: ClientPortal, canActivate: [RoleGuard], data: { roles: ['CLIENT'] } },
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
      // 'hardware' now reuses the shared hrm/asset backend (AssetController /api/hr/assets)
      // rather than a dedicated ITAM controller - see components/hardware/hardware.ts note.
      { path: 'hardware', component: Hardware },
      { path: 'software', component: Software },
      { path: 'assignments', component: Assignments },
      { path: 'offboarding', component: Offboarding },
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
  {
    path: 'project',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/project/project.routes').then(m => m.PROJECT_ROUTES)
  },
  {
    path: 'task',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/task/task.routes').then(m => m.TASK_ROUTES)
  },
  {
    path: 'meeting',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/meeting/meeting.routes').then(m => m.MEETING_ROUTES)
  },
  {
    path: 'company/subscription',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: SubscriptionPage },
      { path: 'success', component: SubscriptionSuccess },
      { path: 'fail', component: SubscriptionFail },
      { path: 'cancel', component: SubscriptionCancel },
    ]
  },
  {
    path: 'company/settings',
    component: CompanySettings,
    canActivate: [AuthGuard],
  },
  {
    path: '',
    loadChildren: () => import('./modules/site/site.routes').then(m => m.SITE_ROUTES)
  },
  { path: '**', component: NotFound }
];
