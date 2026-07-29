import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServicePackageService } from '../../../servicedesk/services/service-package.service';
import { PackageSubscription } from '../../../servicedesk/models/servicedesk.model';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

import { BosCurrencyPipe } from '../../../../shared/pipes/bos-currency.pipe';
@Component({
  selector: 'app-client-packages',
  imports: [BosCurrencyPipe, CommonModule, Pagination, Loader, EmptyState],
  templateUrl: './client-packages.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientPackages implements OnInit {
  subscriptions: PackageSubscription[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';

  constructor(
    private packageService: ServicePackageService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    this.packageService.mySubscriptions(this.page).subscribe({
      next: (res) => {
        this.subscriptions = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load your packages';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  statusClass(status: string): string {
    return {
      ACTIVE: 'text-bg-success',
      EXPIRED: 'text-bg-secondary',
      SUSPENDED: 'text-bg-warning',
      CANCELLED: 'text-bg-danger',
      PENDING: 'text-bg-info',
    }[status] || 'text-bg-secondary';
  }

  usagePercent(sub: PackageSubscription): number {
    if (!sub.requestQuota) return 0;
    return Math.min(100, Math.round((sub.requestsUsed / sub.requestQuota) * 100));
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
