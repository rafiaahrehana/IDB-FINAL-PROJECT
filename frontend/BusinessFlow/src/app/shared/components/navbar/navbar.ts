import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationBell } from '../notification-bell/notification-bell';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, FormsModule, NotificationBell],
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
}
