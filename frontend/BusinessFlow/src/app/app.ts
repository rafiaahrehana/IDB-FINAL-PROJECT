import { Component, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { AuthService } from './core/services/auth.service';
import { NotificationService } from './shared/services/notification.service';
import { Navbar } from './shared/components/navbar/navbar';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { PlatformSidebar } from './shared/components/platform-sidebar/platform-sidebar';
import { ImpersonationBanner } from './shared/components/impersonation-banner/impersonation-banner';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AsyncPipe, Navbar, Sidebar, PlatformSidebar, ImpersonationBanner],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './app.scss',
})
export class App {
  readonly sidebarCollapsed = signal(false);
  readonly mobileSidebarOpen = signal(false);

  constructor(
    public auth: AuthService,
    public notifications: NotificationService,
  ) {}

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  toggleMobileSidebar(): void {
    this.mobileSidebarOpen.update(v => !v);
  }

  closeMobileSidebar(): void {
    this.mobileSidebarOpen.set(false);
  }
}
