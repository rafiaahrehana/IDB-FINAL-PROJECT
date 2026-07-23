import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationBell } from '../notification-bell/notification-bell';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, FormsModule, NotificationBell, RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  searchQuery = '';
  breadcrumb: string[] = [];

  constructor(public auth: AuthService, private router: Router) {
    this.router.events.subscribe(() => this.buildBreadcrumb());
    this.buildBreadcrumb();
  }

  private buildBreadcrumb(): void {
    const segments = this.router.url.split('?')[0].split('/').filter(Boolean);
    this.breadcrumb = segments.map((s) =>
      s.replace(/-/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase()),
    );
  }

  toggleSidebar(): void {
    document.body.classList.toggle('sidebar-open');
  }

  goSearch(): void {
    if (this.searchQuery && this.searchQuery.trim().length > 0) {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery.trim() } });
      this.searchQuery = '';
    } else {
      this.router.navigate(['/search']);
    }
  }

  goSearchAi(): void {
    if (this.searchQuery && this.searchQuery.trim().length > 0) {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery.trim(), ai: 'true' } });
      this.searchQuery = '';
    } else {
      this.router.navigate(['/ai']);
    }
  }

  roleLabel(roles: string[] | undefined | null): string {
    const role = roles?.[0];
    if (!role) return '';
    return role
      .toLowerCase()
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ');
  }

  // The client portal has its own profile page (sidebar "Profile Settings" ->
  // /client/profile) - sending clients to the generic /profile
  // page instead would drop them outside the client portal shell entirely.
  // Platform staff (SUPER_ADMIN etc.) have no Employee HR record, so /my-profile
  // (which reads GET /api/employees/me) would just error for them - keep them on
  // the generic /profile page. Tenant users (COMPANY_OWNER/EMPLOYEE) go to
  // /my-profile instead, which now covers everything /profile did plus their HR
  // details (address, education) and stays consistent with the Welcome landing page.
  settingsLink(): string {
    if (this.auth.hasRole('CLIENT')) return '/client/profile';
    if (this.auth.isPlatformUser()) return '/profile';
    return '/my-profile';
  }
}
