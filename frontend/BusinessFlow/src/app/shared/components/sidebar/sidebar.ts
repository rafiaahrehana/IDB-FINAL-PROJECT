import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface NavGroup { label: string; icon: string; items: NavItem[]; roles?: string[]; permissions?: string[]; }
interface NavItem { label: string; link: string; icon: string; permission?: string; }

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  constructor(private auth: AuthService) {}

  get visibleGroups(): NavGroup[] {
    return this.groups.filter(g => {
      if (g.roles && !this.auth.hasAnyRole(g.roles)) return false;
      if (g.permissions && !this.auth.hasAnyPermission(g.permissions)) return false;
      return true;
    }).map(g => ({
      ...g,
      items: g.items.filter(i => !i.permission || this.auth.hasPermission(i.permission))
    }));
  }

  groups: NavGroup[] = [
    {
      label: 'AI',
      icon: 'bi-stars',
      items: [
        { label: 'AI Assistant', link: '/ai', icon: 'bi-chat-square-text' },
        { label: 'Search', link: '/search', icon: 'bi-search' },
        { label: 'AI Settings', link: '/ai/settings', icon: 'bi-gear', permission: 'AI_ADMIN' },
      ]
    },
    {
      label: 'CRM',
      icon: 'bi-graph-up-arrow',
      permissions: ['CLIENT_VIEW', 'CLIENT_CREATE', 'CLIENT_UPDATE', 'CLIENT_DELETE'],
      items: [
        { label: 'Leads', link: '/crm/leads', icon: 'bi-person-plus', permission: 'CLIENT_VIEW' },
        { label: 'Pipeline', link: '/crm/pipeline', icon: 'bi-kanban', permission: 'CLIENT_VIEW' },
        { label: 'Accounts', link: '/crm/clients', icon: 'bi-building', permission: 'CLIENT_VIEW' },
      ]
    },
    {
      label: 'Servicedesk',
      icon: 'bi-headset',
      permissions: ['SERVICE_REQUEST_VIEW', 'SERVICE_REQUEST_CREATE', 'SERVICE_REQUEST_ASSIGN', 'SERVICE_REQUEST_APPROVE', 'SERVICE_REQUEST_CLOSE'],
      items: [
        { label: 'Requests', link: '/servicedesk/requests', icon: 'bi-ticket', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Approvals', link: '/servicedesk/approvals', icon: 'bi-clipboard-check', permission: 'SERVICE_REQUEST_APPROVE' },
        { label: 'Categories', link: '/servicedesk/categories', icon: 'bi-tags', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Services', link: '/servicedesk/services', icon: 'bi-grid', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Packages', link: '/servicedesk/packages', icon: 'bi-box-seam', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Templates', link: '/servicedesk/templates', icon: 'bi-file-earmark-ruled', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Workflows', link: '/servicedesk/workflows', icon: 'bi-diagram-2', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Reviews', link: '/servicedesk/reviews', icon: 'bi-star', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Knowledge Base', link: '/servicedesk/kb', icon: 'bi-journal-text', permission: 'SERVICE_REQUEST_VIEW' },
      ]
    },
    {
      label: 'Finance',
      icon: 'bi-cash-coin',
      permissions: ['EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_APPROVE', 'PAYROLL_VIEW', 'PAYROLL_PROCESS'],
      items: [
        { label: 'Invoices', link: '/finance/invoices', icon: 'bi-receipt', permission: 'EXPENSE_VIEW' },
        { label: 'Payment Receipts', link: '/finance/payment-receipts', icon: 'bi-receipt-cutoff', permission: 'EXPENSE_VIEW' },
        { label: 'General Ledger', link: '/finance/general-ledger', icon: 'bi-journal-text', permission: 'EXPENSE_VIEW' },
        { label: 'Bank Reconciliation', link: '/finance/bank-reconciliation', icon: 'bi-bank', permission: 'EXPENSE_VIEW' },
        { label: 'Expenses', link: '/finance/expenses', icon: 'bi-cash-stack', permission: 'EXPENSE_VIEW' },
        { label: 'Journal Entries', link: '/finance/journal-entries', icon: 'bi-journal-plus', permission: 'EXPENSE_CREATE' },
        { label: 'Chart of Accounts', link: '/finance/coa', icon: 'bi-journal-bookmark', permission: 'EXPENSE_VIEW' },
        { label: 'Wallet', link: '/finance/wallet', icon: 'bi-wallet2', permission: 'EXPENSE_VIEW' },
        { label: 'Pay Online', link: '/finance/sslcommerz/checkout', icon: 'bi-credit-card', permission: 'EXPENSE_VIEW' },
        { label: 'Reports', link: '/finance/reports', icon: 'bi-bar-chart', permission: 'EXPENSE_VIEW' },
      ]
    },
    {
      label: 'Support',
      icon: 'bi-life-preserver',
      permissions: ['SERVICE_REQUEST_VIEW', 'SERVICE_REQUEST_CREATE'],
      items: [
        { label: 'Tickets', link: '/support/tickets', icon: 'bi-chat-left-dots', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Agents', link: '/support/agents', icon: 'bi-person-badge', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Messages', link: '/support/messages', icon: 'bi-chat-dots', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Categories', link: '/support/categories', icon: 'bi-tags', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'SLA Policies', link: '/support/sla-policies', icon: 'bi-stopwatch', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Context Switches', link: '/support/context-switches', icon: 'bi-arrow-left-right', permission: 'SERVICE_REQUEST_VIEW' },
        { label: 'Audit Logs', link: '/support/audit-logs', icon: 'bi-shield-check', permission: 'SERVICE_REQUEST_VIEW' },
      ]
    },
    {
      label: 'IT Assets',
      icon: 'bi-pc-display',
      items: [
        { label: 'Hardware', link: '/itam/hardware', icon: 'bi-laptop' },
        { label: 'Software', link: '/itam/software', icon: 'bi-file-earmark-code' },
        { label: 'Assignments', link: '/itam/assignments', icon: 'bi-clipboard-data' },
        { label: 'Offboarding', link: '/itam/offboarding', icon: 'bi-person-x' },
      ]
    },
    {
      label: 'HRM',
      icon: 'bi-people',
      permissions: ['EMPLOYEE_VIEW', 'EMPLOYEE_CREATE', 'EMPLOYEE_UPDATE', 'EMPLOYEE_DELETE', 'DEPARTMENT_VIEW', 'LEAVE_VIEW', 'LEAVE_APPROVE', 'PAYROLL_VIEW'],
      items: [
        { label: 'Employees', link: '/hrm/employees', icon: 'bi-person-vcard', permission: 'EMPLOYEE_VIEW' },
        { label: 'Departments', link: '/hrm/departments', icon: 'bi-diagram-3', permission: 'DEPARTMENT_VIEW' },
        { label: 'Designations', link: '/hrm/designations', icon: 'bi-award', permission: 'DEPARTMENT_VIEW' },
        { label: 'Shifts', link: '/hrm/shifts', icon: 'bi-clock-history', permission: 'ATTENDANCE_VIEW' },
        { label: 'Announcements', link: '/hrm/announcements', icon: 'bi-megaphone', permission: 'EMPLOYEE_VIEW' },
        { label: 'Holidays', link: '/hrm/holidays', icon: 'bi-calendar-event', permission: 'EMPLOYEE_VIEW' },
        { label: 'Leaves', link: '/hrm/leaves', icon: 'bi-calendar-minus', permission: 'LEAVE_VIEW' },
        { label: 'Leave Policies', link: '/hrm/leave-policies', icon: 'bi-shield-check', permission: 'LEAVE_VIEW' },
        { label: 'Payroll', link: '/hrm/payroll', icon: 'bi-cash-coin', permission: 'PAYROLL_VIEW' },
        { label: 'Salary Structures', link: '/hrm/salary-structures', icon: 'bi-wallet2', permission: 'PAYROLL_VIEW' },
        { label: 'HR Expenses', link: '/hrm/expenses', icon: 'bi-receipt-cutoff', permission: 'EXPENSE_VIEW' },
        { label: 'HR Assets', link: '/hrm/assets', icon: 'bi-box-seam', permission: 'EMPLOYEE_VIEW' },
        { label: 'Performance', link: '/hrm/performance', icon: 'bi-graph-up', permission: 'EMPLOYEE_VIEW' },
        { label: 'Job Postings', link: '/hrm/job-postings', icon: 'bi-briefcase', permission: 'EMPLOYEE_VIEW' },
        { label: 'Applications', link: '/hrm/applications', icon: 'bi-person-lines-fill', permission: 'EMPLOYEE_VIEW' },
        { label: 'Letters', link: '/hrm/letters', icon: 'bi-envelope-paper', permission: 'EMPLOYEE_VIEW' },
      ]
    },
    {
      label: 'Attendance',
      icon: 'bi-calendar2-check',
      permissions: ['ATTENDANCE_VIEW', 'ATTENDANCE_MARK', 'ATTENDANCE_UPDATE'],
      items: [
        { label: 'Check In/Out', link: '/attendance/check-in', icon: 'bi-box-arrow-in-right', permission: 'ATTENDANCE_MARK' },
        { label: 'Records', link: '/attendance/records', icon: 'bi-calendar-check', permission: 'ATTENDANCE_VIEW' },
        { label: 'Timesheets', link: '/attendance/timesheets', icon: 'bi-clock', permission: 'ATTENDANCE_VIEW' },
        { label: 'Shift Assignments', link: '/attendance/shift-assignments', icon: 'bi-diagram-3', permission: 'ATTENDANCE_VIEW' },
        { label: 'Leaves', link: '/attendance/leaves', icon: 'bi-calendar-x', permission: 'LEAVE_VIEW' },
        { label: 'Biometric Data', link: '/attendance/biometric-data', icon: 'bi-fingerprint', permission: 'ATTENDANCE_VIEW' },
        { label: 'Reports', link: '/attendance/reports', icon: 'bi-file-earmark-bar-graph', permission: 'ATTENDANCE_VIEW' },
      ]
    },
    {
      label: 'Projects',
      icon: 'bi-kanban',
      items: [
        { label: 'Projects', link: '/project', icon: 'bi-kanban' },
        { label: 'Tasks', link: '/task', icon: 'bi-check2-square' },
        { label: 'Meetings', link: '/meeting', icon: 'bi-calendar-event' },
      ]
    },
    {
      label: 'My Portal',
      icon: 'bi-person-circle',
      roles: ['EMPLOYEE', 'CLIENT', 'COMPANY_OWNER'],
      items: [
        { label: 'Dashboard', link: '/dashboard/employee', icon: 'bi-speedometer2' },
        { label: 'My Profile', link: '/dashboard/employee/profile', icon: 'bi-person' },
        { label: 'My Tasks', link: '/dashboard/employee/tasks', icon: 'bi-check2-square' },
        { label: 'My Leaves', link: '/dashboard/employee/leaves', icon: 'bi-calendar-minus' },
        { label: 'My Payroll', link: '/dashboard/employee/payroll', icon: 'bi-cash-coin' },
        { label: 'My Support', link: '/support/client-portal', icon: 'bi-life-preserver' },
      ]
    },
    {
      label: 'Platform Admin',
      icon: 'bi-shield-lock',
      roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'],
      items: [
        { label: 'Companies', link: '/platform/companies', icon: 'bi-buildings' },
        { label: 'Platform Users', link: '/platform/platform-users', icon: 'bi-person-badge' },
        { label: 'Custom Roles', link: '/platform/custom-roles', icon: 'bi-shield' },
        { label: 'Feature Flags', link: '/platform/feature-flags', icon: 'bi-flag' },
        { label: 'Locations Master', link: '/platform/locations', icon: 'bi-geo-alt' },
        { label: 'Platform Expenses', link: '/platform/platform-expenses', icon: 'bi-cash-stack' },
        { label: 'Subscriptions', link: '/platform/subscriptions', icon: 'bi-credit-card-2-front' },
      ]
    },
    {
      label: 'Company',
      icon: 'bi-building',
      roles: ['COMPANY_OWNER'],
      items: [
        { label: 'Subscription', link: '/company/subscription', icon: 'bi-credit-card-2-front' },
        { label: 'Settings', link: '/company/settings', icon: 'bi-gear' },
      ]
    },
  ];
}
