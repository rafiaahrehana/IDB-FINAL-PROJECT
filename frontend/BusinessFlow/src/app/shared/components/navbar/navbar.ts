import { Component, signal, output, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationBell } from '../notification-bell/notification-bell';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [FormsModule, RouterLink, NotificationBell],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Navbar {
  readonly toggleMobileSidebar = output<void>();

  searchQuery = '';
  showUserMenu = signal(false);
  showSearch = signal(false);

  get currentUser() { return this.auth.getCurrentUser(); }

  constructor(
    public auth: AuthService,
    private router: Router
  ) {}

  onToggleMobileSidebar(): void {
    this.toggleMobileSidebar.emit();
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery.trim() } });
      this.searchQuery = '';
      this.showSearch.set(false);
    }
  }

  onSearchAi(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery.trim(), ai: 'true' } });
      this.searchQuery = '';
    } else {
      this.router.navigate(['/ai']);
    }
  }

  toggleUserMenu(): void {
    this.showUserMenu.update(v => !v);
  }

  closeUserMenu(): void {
    this.showUserMenu.set(false);
  }

  onLogout(): void {
    this.closeUserMenu();
    this.auth.logout();
  }

  getInitials(fullName?: string): string {
    if (!fullName) return 'U';
    const parts = fullName.trim().split(/\s+/);
    if (parts.length === 1) return parts[0][0]?.toUpperCase() || 'U';
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }
}
