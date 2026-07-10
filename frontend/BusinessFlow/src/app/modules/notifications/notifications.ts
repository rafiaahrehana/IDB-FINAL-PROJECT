import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Notification } from '../../core/models/notification.model';
import { NotificationService } from '../../core/services/notification.service';
import { Pagination } from '../../shared/components/pagination/pagination';
import { Loader } from '../../shared/components/loader/loader';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { PagedResponse } from '../../core/services/api.service';

@Component({
  selector: 'app-notifications',
  imports: [CommonModule, RouterLink, Pagination, Loader, EmptyState],
  templateUrl: './notifications.html',
})
export class Notifications implements OnInit {
  // VARIABLES
  notifications: Notification[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  filter: 'all' | 'unread' = 'all';

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD NOTIFICATIONS
  load(): void {
    this.loading = true;
    this.error = '';
    this.notificationService.list(this.filter === 'unread', this.page).subscribe({
      next: (res: PagedResponse<Notification>) => { this.notifications = res.content; this.totalPages = res.totalPages; this.loading = false; },
      error: () => { this.error = 'Failed to load notifications'; this.loading = false; }
    });
  }

  // SWITCH FILTER
  setFilter(f: 'all' | 'unread'): void {
    if (this.filter === f) return;
    this.filter = f;
    this.page = 0;
    this.load();
  }

  // OPEN A NOTIFICATION
  openNotification(n: Notification): void {
    if (!n.read) {
      this.notificationService.markAsRead(n.id).subscribe({
        next: () => { n.read = true; this.notificationService.refreshCount(); }
      });
    }
    if (n.actionUrl) this.router.navigateByUrl(n.actionUrl);
  }

  // MARK ALL AS READ
  markAllRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => { this.notificationService.refreshCount(); this.load(); }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // TYPE BADGE COLOR
  typeBadgeClass(type: string): string {
    if (['REJECTED', 'CANCELLED', 'SLA_BREACHED', 'LEAVE_REJECTED'].includes(type)) return 'text-bg-danger';
    if (['SLA_WARNING', 'PAYMENT_DUE'].includes(type)) return 'text-bg-warning';
    if (['COMPLETED', 'LEAVE_APPROVED', 'PAYMENT_RECEIVED'].includes(type)) return 'text-bg-success';
    if (['INVOICE_GENERATED', 'REQUEST_UPDATED', 'REQUEST_ASSIGNED'].includes(type)) return 'text-bg-primary';
    return 'text-bg-secondary';
  }
}
