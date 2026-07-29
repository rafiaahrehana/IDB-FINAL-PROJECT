import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { PermissionService, PermissionCode } from '../../../core/services/permission.service';

import { PortalService } from '../../../modules/portal/portal.service';

interface NavGroup { label: string; icon: string; items: NavItem[]; roles?: string[]; }

interface NavItem { label: string; link: string; icon: string; roles?: string[]; requiredPermission?: PermissionCode; }

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  isOwner = false;
  subscriptionPlan = 'FREE';

  // Groups start collapsed (headers only); the group of the current page auto-expands.
  expandedGroups = new Set<string>();

  constructor(
    private auth: AuthService,
    private permissions: PermissionService,
    private cdr: ChangeDetectorRef,
    private portalService: PortalService,
    private router: Router,
  ) {

    this.permissions.permissions$.subscribe(() => { this.syncActiveGroup(this.router.url); this.cdr.markForCheck(); });
    this.permissions.catalog$.subscribe(() => this.cdr.markForCheck());
    this.auth.currentUser$.subscribe((user) => {
      this.isOwner = this.auth.hasRole('COMPANY_OWNER');
      if (this.isOwner) {
        this.loadCompanyPlan();
      }
      this.cdr.markForCheck();
    });

    // Keep the active page's group open as the user navigates.
    this.syncActiveGroup(this.router.url);
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e) => this.syncActiveGroup((e as NavigationEnd).urlAfterRedirects));
  }

  toggleGroup(label: string): void {
    if (this.expandedGroups.has(label)) this.expandedGroups.delete(label);
    else this.expandedGroups.add(label);
  }

  isExpanded(label: string): boolean {
    return this.expandedGroups.has(label);
  }

  // Expand (never auto-collapse) whichever group contains the current route.
  private syncActiveGroup(url: string): void {
    const active = this.visibleGroups.find((g) => g.items.some((i) => url.startsWith(i.link)));
    if (active && !this.expandedGroups.has(active.label)) {
      this.expandedGroups.add(active.label);
      this.cdr.markForCheck();
    }
  }


  get showDashboardLink(): boolean {
    if (this.isOwner) return true;
    return !this.auth.hasRole('EMPLOYEE');
  }

  get showEmployeeDashboardLink(): boolean {
    return this.auth.hasRole('EMPLOYEE') && !this.isOwner;
  }

  loadCompanyPlan(): void {
    this.portalService.getMyCompany().subscribe({
      next: (c: any) => {
        this.subscriptionPlan = c.subscriptionPlan || 'FREE';
        this.cdr.markForCheck();
      },
      error: () => {
        this.subscriptionPlan = 'FREE';
        this.cdr.markForCheck();
      }
    });
  }

  get visibleGroups(): NavGroup[] {
    return this.groups
      .filter(g => !g.roles || this.auth.hasAnyRole(g.roles))
      .map(g => ({
        ...g,
        items: g.items.filter(i =>
          (!i.roles || this.auth.hasAnyRole(i.roles)) &&
          this.permissions.hasPermission(i.requiredPermission),
        ),
      }))
      .filter(g => g.items.length > 0);
  }

  groups: NavGroup[] = [
    {
      label: 'AI',
      icon: 'bi-stars',
      items: [
        { label: 'AI Settings', link: '/ai/settings', icon: 'bi-gear', requiredPermission: 'AI_ADMIN' },
      ]
    },
    {
      label: 'CRM',
      icon: 'bi-graph-up-arrow',
      items: [
        { label: 'Dashboard', link: '/crm/dashboard', icon: 'bi-speedometer2', requiredPermission: 'OPPORTUNITY_VIEW' },
        { label: 'Leads', link: '/crm/leads', icon: 'bi-person-plus', requiredPermission: 'LEAD_VIEW' },
        { label: 'Opportunities', link: '/crm/pipeline', icon: 'bi-kanban', requiredPermission: 'OPPORTUNITY_VIEW' },
        { label: 'Clients', link: '/crm/clients', icon: 'bi-building', requiredPermission: 'CLIENT_VIEW' },
        { label: 'Contacts', link: '/crm/contacts', icon: 'bi-person-lines-fill', requiredPermission: 'CONTACT_VIEW' },
      ]
    },
    {
      label: 'Servicedesk',
      icon: 'bi-headset',
      items: [
        { label: 'Requests', link: '/servicedesk/requests', icon: 'bi-ticket', requiredPermission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Approvals', link: '/servicedesk/approvals', icon: 'bi-clipboard-check', requiredPermission: 'SERVICE_REQUEST_APPROVE' },
        { label: 'Categories', link: '/servicedesk/categories', icon: 'bi-tags', requiredPermission: 'SERVICE_CATEGORY_VIEW' },
        { label: 'Services', link: '/servicedesk/services', icon: 'bi-grid', requiredPermission: 'SERVICE_CATALOG_VIEW' },
        { label: 'Packages', link: '/servicedesk/packages', icon: 'bi-box-seam', requiredPermission: 'SERVICE_PACKAGE_VIEW' },
        { label: 'Templates', link: '/servicedesk/templates', icon: 'bi-file-earmark-ruled', requiredPermission: 'SERVICE_TEMPLATE_VIEW' },
        { label: 'Workflows', link: '/servicedesk/workflows', icon: 'bi-diagram-2', requiredPermission: 'WORKFLOW_VIEW' },
        { label: 'Reviews', link: '/servicedesk/reviews', icon: 'bi-star', requiredPermission: 'REVIEW_VIEW' },
        { label: 'Knowledge Base', link: '/servicedesk/kb', icon: 'bi-journal-text', requiredPermission: 'KNOWLEDGE_BASE_VIEW' },
      ]
    },
    {
      label: 'HRM',
      icon: 'bi-people',
      items: [
        { label: 'Employees', link: '/hrm/employees', icon: 'bi-person-vcard', requiredPermission: 'EMPLOYEE_VIEW' },
        { label: 'Departments', link: '/hrm/departments', icon: 'bi-diagram-3', requiredPermission: 'DEPARTMENT_VIEW' },
        { label: 'Designations', link: '/hrm/designations', icon: 'bi-award', requiredPermission: 'DESIGNATION_VIEW' },
        { label: 'Roles & Permissions', link: '/roles-permissions', icon: 'bi-shield-lock', roles: ['COMPANY_OWNER'] },
        { label: 'Shifts', link: '/hrm/shifts', icon: 'bi-clock-history', requiredPermission: 'SHIFT_VIEW' },
        { label: 'Announcements', link: '/hrm/announcements', icon: 'bi-megaphone', requiredPermission: 'ANNOUNCEMENT_VIEW' },
        { label: 'Holidays', link: '/hrm/holidays', icon: 'bi-calendar-event', requiredPermission: 'HOLIDAY_VIEW' },
        { label: 'Payroll', link: '/hrm/payroll', icon: 'bi-cash-coin', requiredPermission: 'PAYROLL_VIEW' },
        { label: 'Salary Structures', link: '/hrm/salary-structures', icon: 'bi-wallet2', requiredPermission: 'SALARY_STRUCTURE_VIEW' },
        { label: 'HR Expenses', link: '/hrm/expenses', icon: 'bi-receipt-cutoff', requiredPermission: 'EXPENSE_VIEW' },
        { label: 'Performance', link: '/hrm/performance', icon: 'bi-graph-up', requiredPermission: 'PERFORMANCE_VIEW' },
        { label: 'Job Postings', link: '/hrm/job-postings', icon: 'bi-briefcase', requiredPermission: 'JOB_POSTING_VIEW' },
        { label: 'Applications', link: '/hrm/applications', icon: 'bi-person-lines-fill', requiredPermission: 'APPLICATION_VIEW' },
        { label: 'Letters', link: '/hrm/letters', icon: 'bi-envelope-paper', requiredPermission: 'LETTER_VIEW' },
      ]
    },
    {
      label: 'Attendance',
      icon: 'bi-calendar2-check',
      items: [
        { label: 'Check In/Out', link: '/attendance/check-in', icon: 'bi-box-arrow-in-right', requiredPermission: 'ATTENDANCE_MARK' },
        { label: 'Records', link: '/attendance/records', icon: 'bi-calendar-check', requiredPermission: 'ATTENDANCE_VIEW' },
        { label: 'Timesheets', link: '/attendance/timesheets', icon: 'bi-clock', requiredPermission: 'TIMESHEET_VIEW' },
        { label: 'Shift Assignments', link: '/attendance/shift-assignments', icon: 'bi-diagram-3', requiredPermission: 'SHIFT_ASSIGNMENT_VIEW' },
        { label: 'Leaves', link: '/hrm/leaves', icon: 'bi-calendar-minus', requiredPermission: 'LEAVE_VIEW' },
        { label: 'Leave Balances', link: '/hrm/leave-balances', icon: 'bi-wallet2', requiredPermission: 'LEAVE_BALANCE_VIEW' },
        { label: 'Leave Policies', link: '/hrm/leave-policies', icon: 'bi-shield-check', requiredPermission: 'LEAVE_POLICY_VIEW' },
        { label: 'Biometric Data', link: '/attendance/biometric-data', icon: 'bi-fingerprint', requiredPermission: 'BIOMETRIC_VIEW' },
        { label: 'Reports', link: '/attendance/reports', icon: 'bi-file-earmark-bar-graph', requiredPermission: 'ATTENDANCE_VIEW' },
      ]
    },
    {
      label: 'Finance',
      icon: 'bi-cash-coin',
      items: [
        // Configuration first, then documents, then banking, then reports - mirrors
        // the finance-workspace layout the owner asked for.
        { label: 'Fiscal Years', link: '/finance/fiscal-years', icon: 'bi-calendar3', requiredPermission: 'ACCOUNTING_PERIOD_VIEW' },
        { label: 'Accounting Periods', link: '/finance/accounting-periods', icon: 'bi-calendar-check', requiredPermission: 'ACCOUNTING_PERIOD_VIEW' },
        { label: 'Chart of Accounts', link: '/finance/coa', icon: 'bi-journal-bookmark', requiredPermission: 'CHART_OF_ACCOUNT_VIEW' },
        { label: 'Journal Entries', link: '/finance/journal-entries', icon: 'bi-journal-plus', requiredPermission: 'JOURNAL_ENTRY_VIEW' },
        { label: 'Invoices', link: '/finance/invoices', icon: 'bi-receipt', requiredPermission: 'INVOICE_VIEW' },
        { label: 'Vendor Bills', link: '/finance/vendor-bills', icon: 'bi-file-earmark-text', requiredPermission: 'VENDOR_BILL_VIEW' },
        { label: 'Vendors', link: '/finance/vendors', icon: 'bi-shop', requiredPermission: 'VENDOR_VIEW' },
        { label: 'Payment Receipts', link: '/finance/payment-receipts', icon: 'bi-receipt-cutoff', requiredPermission: 'PAYMENT_RECEIPT_VIEW' },
        { label: 'Refunds', link: '/finance/refunds', icon: 'bi-arrow-counterclockwise', requiredPermission: 'INVOICE_VIEW' },
        { label: 'Expenses', link: '/finance/expenses', icon: 'bi-cash-stack', requiredPermission: 'EXPENSE_VIEW' },
        { label: 'Budgets', link: '/finance/budgets', icon: 'bi-piggy-bank', requiredPermission: 'BUDGET_VIEW' },
        { label: 'Fixed Assets', link: '/finance/fixed-assets', icon: 'bi-pc-display-horizontal', requiredPermission: 'FIXED_ASSET_VIEW' },
        { label: 'Wallet', link: '/finance/wallet', icon: 'bi-wallet2', requiredPermission: 'WALLET_VIEW' },
        { label: 'Bank Reconciliation', link: '/finance/bank-reconciliation', icon: 'bi-bank', requiredPermission: 'BANK_RECONCILIATION_VIEW' },
        { label: 'General Ledger', link: '/finance/general-ledger', icon: 'bi-journal-text', requiredPermission: 'GENERAL_LEDGER_VIEW' },
        { label: 'Reports', link: '/finance/reports', icon: 'bi-bar-chart', requiredPermission: 'FINANCIAL_REPORT_VIEW' },
        { label: 'Billing Settings', link: '/finance/company-settings', icon: 'bi-building-gear', requiredPermission: 'COMPANY_SETTINGS' },
      ]
    },
    {
      label: 'Platform Support',
      icon: 'bi-life-preserver',
      items: [
        { label: 'Platform Tickets', link: '/support/tickets', icon: 'bi-chat-left-dots', requiredPermission: 'TICKET_VIEW' },
        { label: 'Direct Messages', link: '/support/messages', icon: 'bi-chat-dots', requiredPermission: 'SUPPORT_MESSAGE_VIEW' },
        { label: 'SLA Policies', link: '/support/sla-policies', icon: 'bi-stopwatch', requiredPermission: 'SLA_POLICY_VIEW' },
        { label: 'Audit Logs', link: '/support/audit-logs', icon: 'bi-shield-check', requiredPermission: 'AUDIT_LOG_VIEW' },
      ]
    },
    {
      label: 'IT Assets',
      icon: 'bi-pc-display',
      items: [
        { label: 'Hardware', link: '/itam/hardware', icon: 'bi-laptop', requiredPermission: 'HARDWARE_VIEW' },
        { label: 'Software', link: '/itam/software', icon: 'bi-file-earmark-code', requiredPermission: 'SOFTWARE_LICENSE_VIEW' },
        { label: 'Assignments', link: '/itam/assignments', icon: 'bi-clipboard-data', requiredPermission: 'ASSET_ASSIGNMENT_VIEW' },
        { label: 'Offboarding', link: '/itam/offboarding', icon: 'bi-person-x', requiredPermission: 'OFFBOARDING_VIEW' },
        { label: 'Bulk Import', link: '/itam/import', icon: 'bi-upload', requiredPermission: 'ASSET_IMPORT_VIEW' },
      ]
    },
  ];
}
