import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { ClientHomeRedirectGuard } from './core/guards/client-home-redirect.guard';
import { DashboardAccessGuard } from './core/guards/dashboard-access.guard';
import { DashboardComponent } from './modules/dashboard/components/dashboard.component';
import { EmployeeDashboard } from './modules/dashboard/components/employee-dashboard/employee-dashboard';
import { Welcome } from './shared/components/welcome/welcome';
import { MyPayslips } from './modules/hrm/components/my-payslips/my-payslips';
import { Login } from './modules/auth/login/login';
import { Register } from './modules/auth/register/register';
import { ClientRegister } from './modules/auth/client-register/client-register';
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
import { ContactSales } from './modules/landing/components/contact-sales/contact-sales';
import { PaymentResult } from './modules/portal/payment-result/payment-result';

// Finance
import { ChartOfAccounts } from './modules/finance/components/chart-of-accounts/chart-of-accounts';
import { Expenses } from './modules/finance/components/expenses/expenses';
import { Invoices } from './modules/finance/components/invoices/invoices';
import { Refunds } from './modules/finance/components/refunds/refunds';
import { Reports as FinanceReports } from './modules/finance/components/reports/reports';
import { WalletPage } from './modules/finance/components/wallet/wallet';
import { PaymentReceipts } from './modules/finance/components/payment-receipts/payment-receipts';
import { GeneralLedger } from './modules/finance/components/general-ledger/general-ledger';
import { BankReconciliationPage } from './modules/finance/components/bank-reconciliation/bank-reconciliation';
import { JournalEntries } from './modules/finance/components/journal-entries/journal-entries';
import { CompanySettings } from './modules/finance/components/company-settings/company-settings';
import { AccountingPeriods } from './modules/finance/components/accounting-periods/accounting-periods';
import { FiscalYears } from './modules/finance/components/fiscal-years/fiscal-years';
import { Vendors } from './modules/finance/components/vendors/vendors';
import { VendorBills } from './modules/finance/components/vendor-bills/vendor-bills';
import { Budgets } from './modules/finance/components/budgets/budgets';
import { FixedAssets } from './modules/finance/components/fixed-assets/fixed-assets';

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
import { Reports as AttendanceReports } from './modules/attendance/components/reports/reports';
import { BiometricDataPage } from './modules/attendance/components/biometric-data/biometric-data';
import { ShiftAssignments } from './modules/attendance/components/shift-assignments/shift-assignments';
import { Timesheets } from './modules/attendance/components/timesheets/timesheets';

export const routes: Routes = [
  { path: '', component: DashboardComponent, canActivate: [AuthGuard, ClientHomeRedirectGuard, DashboardAccessGuard] },
  { path: 'dashboard', redirectTo: '', pathMatch: 'full' },
  { path: 'my-profile', component: Welcome, canActivate: [AuthGuard] },
  { path: 'employee-dashboard', component: EmployeeDashboard, canActivate: [AuthGuard] },
  { path: 'hrm/my-payslips', component: MyPayslips, canActivate: [AuthGuard] },
  // Public pages - no auth
  { path: 'home', component: Landing },
  { path: 'contact', component: ContactSales },
  { path: 'portal/:subdomain', loadChildren: () => import('./modules/site/site.routes').then(m => m.SITE_ROUTES) },
  { path: 'payment-result', component: PaymentResult },

  {
    path: 'roles-permissions',
    loadComponent: () => import('./modules/roles-permissions/components/roles-permissions/roles-permissions').then(m => m.RolesPermissions),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['COMPANY_OWNER'] },
  },
  { path: 'auth/login', component: Login },
  { path: 'auth/register', component: Register },
  { path: 'auth/register-client', component: ClientRegister },
  { path: 'auth/forgot-password', component: ForgotPassword },
  { path: 'auth/reset-password', component: ResetPassword },
  { path: 'auth/verify-email', component: VerifyEmail },
  { path: 'auth', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'search', component: GlobalSearch, canActivate: [AuthGuard] },
  { path: 'ai', component: AiAssistant, canActivate: [AuthGuard] },
  { path: 'ai/settings', loadComponent: () => import('./modules/ai/components/ai-settings/ai-settings').then(m => m.AiSettings), canActivate: [AuthGuard] },
  { path: 'notifications', component: Notifications, canActivate: [AuthGuard] },
  { path: 'profile', component: NotificationPreferences, canActivate: [AuthGuard] },
  { path: 'notifications/preferences', redirectTo: 'profile', pathMatch: 'full' },
  { path: 'settings/billing', redirectTo: 'finance/wallet', pathMatch: 'full' },
  {
    path: 'crm',
    canActivate: [AuthGuard, RoleGuard],
    // canActivateChild (not just canActivate) so RoleGuard re-checks each individual
    // child's own requiredPermission - without it, moving between two children of an
    // already-active parent (e.g. /crm/leads -> /crm/pipeline) skips guard evaluation
    // entirely, since the parent node itself doesn't get reactivated on lateral moves.
    canActivateChild: [AuthGuard, RoleGuard],
    loadChildren: () => import('./modules/crm/crm.routes').then(m => m.CRM_ROUTES)
  },
  {
    path: 'client',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['CLIENT'] },
    loadChildren: () => import('./modules/client-portal/client-portal.routes').then(m => m.CLIENT_PORTAL_ROUTES)
  },
  {
    path: 'servicedesk',
    canActivate: [AuthGuard, RoleGuard],
    canActivateChild: [AuthGuard, RoleGuard],
    loadChildren: () => import('./modules/servicedesk/servicedesk.routes').then(m => m.SERVICEDESK_ROUTES)
  },
  {
    path: 'hrm',
    canActivate: [AuthGuard, RoleGuard],
    canActivateChild: [AuthGuard, RoleGuard],
    loadChildren: () => import('./modules/hrm/hrm.routes').then(m => m.HRM_ROUTES)
  },
  {
    path: 'platform',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER', 'SUPPORT_AGENT', 'SUPPORT_MANAGER'] },
    loadChildren: () => import('./modules/platform-admin/platform-admin.routes').then(m => m.PLATFORM_ADMIN_ROUTES)
  },
  {
    path: 'subscriptionPlan',
    canActivate: [AuthGuard],
    loadComponent: () => import('./modules/subscription/components/subscription-plan/subscription-plan').then(m => m.SubscriptionPlan)
  },
  {
    path: 'finance',
    canActivate: [AuthGuard, RoleGuard],
    canActivateChild: [AuthGuard, RoleGuard],
    children: [
      { path: 'coa', component: ChartOfAccounts, data: { requiredPermission: 'CHART_OF_ACCOUNT_VIEW' } },
      { path: 'expenses', component: Expenses, data: { requiredPermission: 'EXPENSE_VIEW' } },
      { path: 'invoices', component: Invoices, data: { requiredPermission: 'INVOICE_VIEW' } },
      { path: 'refunds', component: Refunds, data: { requiredPermission: 'INVOICE_VIEW' } },
      { path: 'journal-entries', component: JournalEntries, data: { requiredPermission: 'JOURNAL_ENTRY_VIEW' } },
      { path: 'reports', component: FinanceReports, data: { requiredPermission: 'FINANCIAL_REPORT_VIEW' } },
      { path: 'wallet', component: WalletPage, data: { requiredPermission: 'WALLET_VIEW' } },
      { path: 'payment-receipts', component: PaymentReceipts, data: { requiredPermission: 'PAYMENT_RECEIPT_VIEW' } },
      { path: 'general-ledger', component: GeneralLedger, data: { requiredPermission: 'GENERAL_LEDGER_VIEW' } },
      { path: 'bank-reconciliation', component: BankReconciliationPage, data: { requiredPermission: 'BANK_RECONCILIATION_VIEW' } },
      { path: 'company-settings', component: CompanySettings, data: { requiredPermission: 'COMPANY_SETTINGS' } },
      { path: 'accounting-periods', component: AccountingPeriods, data: { requiredPermission: 'ACCOUNTING_PERIOD_VIEW' } },
      { path: 'fiscal-years', component: FiscalYears, data: { requiredPermission: 'ACCOUNTING_PERIOD_VIEW' } },
      { path: 'vendors', component: Vendors, data: { requiredPermission: 'VENDOR_VIEW' } },
      { path: 'vendor-bills', component: VendorBills, data: { requiredPermission: 'VENDOR_BILL_VIEW' } },
      { path: 'budgets', component: Budgets, data: { requiredPermission: 'BUDGET_VIEW' } },
      { path: 'fixed-assets', component: FixedAssets, data: { requiredPermission: 'FIXED_ASSET_VIEW' } },
      { path: '', redirectTo: 'invoices', pathMatch: 'full' }
    ]
  },
  {
    path: 'support',
    canActivate: [AuthGuard, RoleGuard],
    canActivateChild: [AuthGuard, RoleGuard],
    children: [
      { path: 'tickets', component: Tickets, data: { requiredPermission: 'TICKET_VIEW' } },
      { path: 'categories', component: Categories, data: { requiredPermission: 'SUPPORT_CATEGORY_VIEW' } },
      { path: 'messages', component: Messages, data: { requiredPermission: 'SUPPORT_MESSAGE_VIEW' } },
      { path: 'sla-policies', component: SlaPolicies, data: { requiredPermission: 'SLA_POLICY_VIEW' } },
      { path: 'audit-logs', component: AuditLogs, data: { requiredPermission: 'AUDIT_LOG_VIEW' } },
      { path: 'agents', component: Agents, data: { roles: ['SUPER_ADMIN', 'SUPPORT_MANAGER'] } },
      { path: 'context-switches', component: ContextSwitches },
      { path: '', redirectTo: 'tickets', pathMatch: 'full' }
    ]
  },
  {
    path: 'itam',
    canActivate: [AuthGuard, RoleGuard],
    canActivateChild: [AuthGuard, RoleGuard],
    children: [
      { path: 'hardware', component: Hardware, data: { requiredPermission: 'HARDWARE_VIEW' } },
      { path: 'software', component: Software, data: { requiredPermission: 'SOFTWARE_LICENSE_VIEW' } },
      { path: 'assignments', component: Assignments, data: { requiredPermission: 'ASSET_ASSIGNMENT_VIEW' } },
      { path: 'offboarding', component: Offboarding, data: { requiredPermission: 'OFFBOARDING_VIEW' } },
      { path: 'import', component: AssetImport, data: { requiredPermission: 'ASSET_IMPORT_VIEW' } },
      { path: '', redirectTo: 'hardware', pathMatch: 'full' }
    ]
  },
  {
    path: 'attendance',
    canActivate: [AuthGuard, RoleGuard],
    canActivateChild: [AuthGuard, RoleGuard],
    children: [
      { path: 'check-in', component: CheckInOut, data: { requiredPermission: 'ATTENDANCE_MARK' } },
      { path: 'records', component: AttendanceList, data: { requiredPermission: 'ATTENDANCE_VIEW' } },
      { path: 'timesheets', component: Timesheets, data: { requiredPermission: 'TIMESHEET_VIEW' } },
      { path: 'shift-assignments', component: ShiftAssignments, data: { requiredPermission: 'SHIFT_ASSIGNMENT_VIEW' } },
      { path: 'biometric-data', component: BiometricDataPage, data: { requiredPermission: 'BIOMETRIC_VIEW' } },
      { path: 'reports', component: AttendanceReports, data: { requiredPermission: 'ATTENDANCE_VIEW' } },
      { path: '', redirectTo: 'check-in', pathMatch: 'full' }
    ]
  },
  { path: 'forbidden', component: Forbidden },
  { path: 'server-error', component: ServerError },
  { path: '**', component: NotFound }
];