import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './core/services/auth.service';
import { NotificationService } from './shared/services/notification.service';
import { PermissionService } from './core/services/permission.service';
import { Navbar } from './shared/components/navbar/navbar';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { PlatformSidebar } from './shared/components/platform-sidebar/platform-sidebar';
import { ClientSidebar } from './shared/components/client-sidebar/client-sidebar';
import { ImpersonationBanner } from './shared/components/impersonation-banner/impersonation-banner';

function isPortalUrl(url: string): boolean {
  return url.startsWith('/portal/');
}

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, Navbar, Sidebar, PlatformSidebar, ClientSidebar, ImpersonationBanner],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './app.scss',
})
export class App {
  private router = inject(Router);

  // Seed from window.location, not router.url: on a fresh bootstrap (e.g. this app
  // reloaded inside its own iframe for the website preview) router.url is still its
  // default '/' until the first NavigationEnd fires, so a portal URL would briefly -
  // sometimes persistently, if that first navigation is slow - render the full
  // authenticated shell (navbar+sidebar) around the embedded portal page.
  isPortalRoute = signal(isPortalUrl(window.location.pathname));

  constructor(
    public auth: AuthService,
    public notifications: NotificationService,
    public permissionService: PermissionService,
  ) {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe((e) => {
      this.isPortalRoute.set(isPortalUrl((e as NavigationEnd).urlAfterRedirects));
    });
  }

  goToAi(): void {
    this.router.navigate(['/ai']);
  }
}
