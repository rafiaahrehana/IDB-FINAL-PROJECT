import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface NavGroup { label: string; icon: string; items: NavItem[]; roles?: string[]; }
interface NavItem { label: string; link: string; icon: string; }

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  constructor(private auth: AuthService) {}

  get visibleGroups(): NavGroup[] {
    return this.groups.filter(g => !g.roles || this.auth.hasAnyRole(g.roles));
  }

  groups: NavGroup[] = [
    {
      label: 'Portal',
      icon: 'bi-globe',
      roles: ['COMPANY_OWNER'],
      items: [
        { label: 'Website View', link: '/website-view', icon: 'bi-globe' },
        { label: 'Portal Settings', link: '/portal-settings', icon: 'bi-brush' },
      ]
    },
    {
      label: 'AI',
      icon: 'bi-stars',
      items: [
        { label: 'AI Assistant', link: '/ai', icon: 'bi-chat-square-text' },
        { label: 'AI Settings', link: '/ai/settings', icon: 'bi-gear' },
      ]
    },
    {
      label: 'CRM',
      icon: 'bi-graph-up-arrow',
      items: [
        { label: 'Leads', link: '/crm/leads', icon: 'bi-person-plus' },
        { label: 'Pipeline', link: '/crm/pipeline', icon: 'bi-kanban' },
        { label: 'Accounts', link: '/crm/clients', icon: 'bi-building' },
      ]
    },
    {
      label: 'Servicedesk',
      icon: 'bi-headset',
      items: [
        { label: 'Requests', link: '/servicedesk/requests', icon: 'bi-ticket' },
        { label: 'Approvals', link: '/servicedesk/approvals', icon: 'bi-clipboard-check' },
        { label: 'Categories', link: '/servicedesk/categories', icon: 'bi-tags' },
        { label: 'Services', link: '/servicedesk/services', icon: 'bi-grid' },
        { label: 'Packages', link: '/servicedesk/packages', icon: 'bi-box-seam' },
        { label: 'Templates', link: '/servicedesk/templates', icon: 'bi-file-earmark-ruled' },
        { label: 'Workflows', link: '/servicedesk/workflows', icon: 'bi-diagram-2' },
        { label: 'Reviews', link: '/servicedesk/reviews', icon: 'bi-star' },
        { label: 'Knowledge Base', link: '/servicedesk/kb', icon: 'bi-journal-text' },
      ]
    },
    {
      label: 'Finance',
      icon: 'bi-cash-coin',
      items: [
        { label: 'Invoices', link: '/finance/invoices', icon: 'bi-receipt' },
        { label: 'Payment Receipts', link: '/finance/payment-receipts', icon: 'bi-receipt-cutoff' },
        { label: 'General Ledger', link: '/finance/general-ledger', icon: 'bi-journal-text' },
        { label: 'Bank Reconciliation', link: '/finance/bank-reconciliation', icon: 'bi-bank' },
        { label: 'Expenses', link: '/finance/expenses', icon: 'bi-cash-stack' },
        { label: 'Journal Entries', link: '/finance/journal-entries', icon: 'bi-journal-plus' },
        { label: 'Chart of Accounts', link: '/finance/coa', icon: 'bi-journal-bookmark' },
        { label: 'Wallet', link: '/finance/wallet', icon: 'bi-wallet2' },
        { label: 'Reports', link: '/finance/reports', icon: 'bi-bar-chart' },
      ]
    },
    {
      label: 'Support',
      icon: 'bi-life-preserver',
      items: [
        { label: 'Tickets', link: '/support/tickets', icon: 'bi-chat-left-dots' },
        { label: 'Agents', link: '/support/agents', icon: 'bi-person-badge' },
        { label: 'Messages', link: '/support/messages', icon: 'bi-chat-dots' },
        { label: 'Categories', link: '/support/categories', icon: 'bi-tags' },
        { label: 'SLA Policies', link: '/support/sla-policies', icon: 'bi-stopwatch' },
        { label: 'Context Switches', link: '/support/context-switches', icon: 'bi-arrow-left-right' },
        { label: 'Audit Logs', link: '/support/audit-logs', icon: 'bi-shield-check' },
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
        { label: 'Bulk Import', link: '/itam/import', icon: 'bi-upload' },
      ]
    },
    {
      label: 'HRM',
      icon: 'bi-people',
      items: [
        { label: 'Employees', link: '/hrm/employees', icon: 'bi-person-vcard' },
        { label: 'Departments', link: '/hrm/departments', icon: 'bi-diagram-3' },
        { label: 'Designations', link: '/hrm/designations', icon: 'bi-award' },
        { label: 'Shifts', link: '/hrm/shifts', icon: 'bi-clock-history' },
        { label: 'Announcements', link: '/hrm/announcements', icon: 'bi-megaphone' },
        { label: 'Holidays', link: '/hrm/holidays', icon: 'bi-calendar-event' },
        { label: 'Leaves', link: '/hrm/leaves', icon: 'bi-calendar-minus' },
        { label: 'Leave Policies', link: '/hrm/leave-policies', icon: 'bi-shield-check' },
        { label: 'Payroll', link: '/hrm/payroll', icon: 'bi-cash-coin' },
        { label: 'Salary Structures', link: '/hrm/salary-structures', icon: 'bi-wallet2' },
        { label: 'HR Expenses', link: '/hrm/expenses', icon: 'bi-receipt-cutoff' },
        { label: 'HR Assets', link: '/hrm/assets', icon: 'bi-box-seam' },
        { label: 'Performance', link: '/hrm/performance', icon: 'bi-graph-up' },
        { label: 'Job Postings', link: '/hrm/job-postings', icon: 'bi-briefcase' },
        { label: 'Applications', link: '/hrm/applications', icon: 'bi-person-lines-fill' },
        { label: 'Letters', link: '/hrm/letters', icon: 'bi-envelope-paper' },
      ]
    },
    {
      label: 'Attendance',
      icon: 'bi-calendar2-check',
      items: [
        { label: 'Check In/Out', link: '/attendance/check-in', icon: 'bi-box-arrow-in-right' },
        { label: 'Records', link: '/attendance/records', icon: 'bi-calendar-check' },
        { label: 'Timesheets', link: '/attendance/timesheets', icon: 'bi-clock' },
        { label: 'Shift Assignments', link: '/attendance/shift-assignments', icon: 'bi-diagram-3' },
        { label: 'Leaves', link: '/attendance/leaves', icon: 'bi-calendar-x' },
        { label: 'Biometric Data', link: '/attendance/biometric-data', icon: 'bi-fingerprint' },
        { label: 'Reports', link: '/attendance/reports', icon: 'bi-file-earmark-bar-graph' },
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
      ]
    },
  ];
}
