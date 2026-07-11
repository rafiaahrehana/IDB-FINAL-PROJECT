import { Component, signal, computed, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RbacService } from '../../../core/services/rbac.service';
import { NavGroup } from '../../../core/models/rbac.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Sidebar {
  readonly collapsed = input(false);
  readonly mobileOpen = input(false);
  readonly closeMobile = output<void>();

  readonly expandedGroups = signal<Set<string>>(new Set(['AI']));

  readonly visibleGroups = computed(() => {
    return this.groups.filter(g => !g.roles || this.rbac.hasAnyRole(g.roles));
  });

  constructor(
    public auth: AuthService,
    private rbac: RbacService
  ) {}

  toggleGroup(label: string): void {
    const expanded = new Set(this.expandedGroups());
    if (expanded.has(label)) {
      expanded.delete(label);
    } else {
      expanded.add(label);
    }
    this.expandedGroups.set(expanded);
  }

  isGroupExpanded(label: string): boolean {
    return this.expandedGroups().has(label);
  }

  onCloseMobile(): void {
    this.closeMobile.emit();
  }

  groups: NavGroup[] = [
    {
      label: 'Portal',
      icon: 'bi-globe',
      roles: ['COMPANY_OWNER'],
      items: [
        { label: 'Portal Settings', route: '/portal-settings', icon: 'bi-brush' },
      ]
    },
    {
      label: 'AI',
      icon: 'bi-stars',
      items: [
        { label: 'AI Assistant', route: '/ai', icon: 'bi-chat-square-text' },
        { label: 'AI Settings', route: '/ai/settings', icon: 'bi-gear' },
      ]
    },
    {
      label: 'CRM',
      icon: 'bi-graph-up-arrow',
      items: [
        { label: 'Leads', route: '/crm/leads', icon: 'bi-person-plus' },
        { label: 'Pipeline', route: '/crm/pipeline', icon: 'bi-kanban' },
        { label: 'Accounts', route: '/crm/clients', icon: 'bi-building' },
      ]
    },
    {
      label: 'Servicedesk',
      icon: 'bi-headset',
      items: [
        { label: 'Requests', route: '/servicedesk/requests', icon: 'bi-ticket' },
        { label: 'Approvals', route: '/servicedesk/approvals', icon: 'bi-clipboard-check' },
        { label: 'Categories', route: '/servicedesk/categories', icon: 'bi-tags' },
        { label: 'Services', route: '/servicedesk/services', icon: 'bi-grid-3x3' },
        { label: 'Packages', route: '/servicedesk/packages', icon: 'bi-box-seam' },
        { label: 'Templates', route: '/servicedesk/templates', icon: 'bi-file-earmark-ruled' },
        { label: 'Workflows', route: '/servicedesk/workflows', icon: 'bi-diagram-2' },
        { label: 'Reviews', route: '/servicedesk/reviews', icon: 'bi-star' },
        { label: 'Knowledge Base', route: '/servicedesk/kb', icon: 'bi-journal-text' },
      ]
    },
    {
      label: 'Finance',
      icon: 'bi-cash-coin',
      items: [
        { label: 'Invoices', route: '/finance/invoices', icon: 'bi-receipt' },
        { label: 'Payment Receipts', route: '/finance/payment-receipts', icon: 'bi-receipt-cutoff' },
        { label: 'General Ledger', route: '/finance/general-ledger', icon: 'bi-journal-text' },
        { label: 'Bank Reconciliation', route: '/finance/bank-reconciliation', icon: 'bi-bank' },
        { label: 'Expenses', route: '/finance/expenses', icon: 'bi-cash-stack' },
        { label: 'Journal Entries', route: '/finance/journal-entries', icon: 'bi-journal-plus' },
        { label: 'Chart of Accounts', route: '/finance/coa', icon: 'bi-journal-bookmark' },
        { label: 'Wallet', route: '/finance/wallet', icon: 'bi-wallet2' },
        { label: 'Reports', route: '/finance/reports', icon: 'bi-bar-chart' },
      ]
    },
    {
      label: 'Support',
      icon: 'bi-life-preserver',
      items: [
        { label: 'Tickets', route: '/support/tickets', icon: 'bi-chat-left-dots' },
        { label: 'Agents', route: '/support/agents', icon: 'bi-person-badge' },
        { label: 'Messages', route: '/support/messages', icon: 'bi-chat-dots' },
        { label: 'Categories', route: '/support/categories', icon: 'bi-tags' },
        { label: 'SLA Policies', route: '/support/sla-policies', icon: 'bi-stopwatch' },
        { label: 'Context Switches', route: '/support/context-switches', icon: 'bi-arrow-left-right' },
        { label: 'Audit Logs', route: '/support/audit-logs', icon: 'bi-shield-check' },
      ]
    },
    {
      label: 'IT Assets',
      icon: 'bi-pc-display',
      items: [
        { label: 'Hardware', route: '/itam/hardware', icon: 'bi-laptop' },
        { label: 'Software', route: '/itam/software', icon: 'bi-file-earmark-code' },
        { label: 'Assignments', route: '/itam/assignments', icon: 'bi-clipboard-data' },
        { label: 'Offboarding', route: '/itam/offboarding', icon: 'bi-person-x' },
        { label: 'Bulk Import', route: '/itam/import', icon: 'bi-upload' },
      ]
    },
    {
      label: 'HRM',
      icon: 'bi-people',
      items: [
        { label: 'Employees', route: '/hrm/employees', icon: 'bi-person-vcard' },
        { label: 'Departments', route: '/hrm/departments', icon: 'bi-diagram-3' },
        { label: 'Designations', route: '/hrm/designations', icon: 'bi-award' },
        { label: 'Shifts', route: '/hrm/shifts', icon: 'bi-clock-history' },
        { label: 'Announcements', route: '/hrm/announcements', icon: 'bi-megaphone' },
        { label: 'Holidays', route: '/hrm/holidays', icon: 'bi-calendar-event' },
        { label: 'Leaves', route: '/hrm/leaves', icon: 'bi-calendar-minus' },
        { label: 'Leave Policies', route: '/hrm/leave-policies', icon: 'bi-shield-check' },
        { label: 'Payroll', route: '/hrm/payroll', icon: 'bi-cash-coin' },
        { label: 'Salary Structures', route: '/hrm/salary-structures', icon: 'bi-wallet2' },
        { label: 'HR Expenses', route: '/hrm/expenses', icon: 'bi-receipt-cutoff' },
        { label: 'HR Assets', route: '/hrm/assets', icon: 'bi-box-seam' },
        { label: 'Performance', route: '/hrm/performance', icon: 'bi-graph-up' },
        { label: 'Job Postings', route: '/hrm/job-postings', icon: 'bi-briefcase' },
        { label: 'Applications', route: '/hrm/applications', icon: 'bi-person-lines-fill' },
        { label: 'Letters', route: '/hrm/letters', icon: 'bi-envelope-paper' },
      ]
    },
    {
      label: 'Attendance',
      icon: 'bi-calendar2-check',
      items: [
        { label: 'Check In/Out', route: '/attendance/check-in', icon: 'bi-box-arrow-in-right' },
        { label: 'Records', route: '/attendance/records', icon: 'bi-calendar-check' },
        { label: 'Timesheets', route: '/attendance/timesheets', icon: 'bi-clock' },
        { label: 'Shift Assignments', route: '/attendance/shift-assignments', icon: 'bi-diagram-3' },
        { label: 'Leaves', route: '/attendance/leaves', icon: 'bi-calendar-x' },
        { label: 'Biometric Data', route: '/attendance/biometric-data', icon: 'bi-fingerprint' },
        { label: 'Reports', route: '/attendance/reports', icon: 'bi-file-earmark-bar-graph' },
      ]
    },
    {
      label: 'Platform Admin',
      icon: 'bi-shield-lock',
      roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'],
      items: [
        { label: 'Companies', route: '/platform/companies', icon: 'bi-buildings' },
        { label: 'Platform Users', route: '/platform/platform-users', icon: 'bi-person-badge' },
        { label: 'Custom Roles', route: '/platform/custom-roles', icon: 'bi-shield' },
        { label: 'Feature Flags', route: '/platform/feature-flags', icon: 'bi-flag' },
        { label: 'Locations Master', route: '/platform/locations', icon: 'bi-geo-alt' },
        { label: 'Platform Expenses', route: '/platform/platform-expenses', icon: 'bi-cash-stack' },
      ]
    },
  ];
}
