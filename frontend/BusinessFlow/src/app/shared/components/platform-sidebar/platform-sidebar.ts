import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface NavGroup { label: string; icon: string; items: NavItem[]; roles?: string[]; }
interface NavItem { label: string; link: string; icon: string; }

// Sidebar for platform (SaaS-provider) staff only - SUPER_ADMIN, SYSTEM_ADMIN,
// SUPPORT_AGENT, SUPPORT_MANAGER, MARKETING_MANAGER, PLATFORM_ACCOUNTANT, SALES_MANAGER.
// Deliberately has NO CRM/Finance/HRM/Servicedesk/ITAM/Attendance groups - those are
// Company (tenant) data and platform staff reach them only via "Access Company"
// impersonation, never as their own identity. See auth.service.ts PLATFORM_ROLES.
@Component({
  selector: 'app-platform-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './platform-sidebar.html',
  styleUrl: './platform-sidebar.scss',
})
export class PlatformSidebar {
  constructor(private auth: AuthService) {}

  get visibleGroups(): NavGroup[] {
    return this.groups.filter(g => !g.roles || this.auth.hasAnyRole(g.roles));
  }

  groups: NavGroup[] = [
    {
      label: 'Companies',
      icon: 'bi-buildings',
      roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER', 'SUPPORT_AGENT', 'SUPPORT_MANAGER'],
      items: [
        { label: 'Companies', link: '/platform/companies', icon: 'bi-buildings' },
      ]
    },
    {
      label: 'Support',
      icon: 'bi-life-preserver',
      roles: ['SUPER_ADMIN', 'SUPPORT_AGENT', 'SUPPORT_MANAGER'],
      items: [
        { label: 'Support Agents', link: '/support/agents', icon: 'bi-person-badge' },
        { label: 'Tickets', link: '/support/tickets', icon: 'bi-chat-left-dots' },
        { label: 'SLA Policies', link: '/support/sla-policies', icon: 'bi-stopwatch' },
      ]
    },
    {
      label: 'Platform Administration',
      icon: 'bi-shield-lock',
      roles: ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER'],
      items: [
        { label: 'Platform Users', link: '/platform/platform-users', icon: 'bi-person-vcard' },
        { label: 'Custom Roles', link: '/platform/custom-roles', icon: 'bi-shield' },
        { label: 'Feature Flags', link: '/platform/feature-flags', icon: 'bi-flag' },
        { label: 'Locations Master', link: '/platform/locations', icon: 'bi-geo-alt' },
      ]
    },
    {
      label: 'Finance',
      icon: 'bi-cash-coin',
      roles: ['SUPER_ADMIN', 'PLATFORM_ACCOUNTANT'],
      items: [
        { label: 'Platform Expenses', link: '/platform/platform-expenses', icon: 'bi-cash-stack' },
      ]
    },
  ];
}
