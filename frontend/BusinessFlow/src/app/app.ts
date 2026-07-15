import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { NotificationService } from './shared/services/notification.service';
import { Navbar } from './shared/components/navbar/navbar';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { PlatformSidebar } from './shared/components/platform-sidebar/platform-sidebar';
import { ClientSidebar } from './shared/components/client-sidebar/client-sidebar';
import { ImpersonationBanner } from './shared/components/impersonation-banner/impersonation-banner';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, Navbar, Sidebar, PlatformSidebar, ClientSidebar, ImpersonationBanner],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './app.scss',
})
export class App {
  constructor(
    public auth: AuthService,
    public notifications: NotificationService,
  ) {}
}
