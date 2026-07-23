import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService, ClientSummary } from '../../../../core/services/dashboard.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../crm/services/client.service';
import { Client } from '../../../crm/models/crm.model';
import { Loader } from '../../../../shared/components/loader/loader';
import { ServicePackageService } from '../../../servicedesk/services/service-package.service';
import { ServiceRequestService } from '../../../servicedesk/services/service-request.service';
import { PackageSubscription, ServicePackage, ServiceRequest } from '../../../servicedesk/models/servicedesk.model';

@Component({
  selector: 'app-client-dashboard',
  imports: [CommonModule, RouterLink, Loader],
  templateUrl: './client-dashboard.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientDashboard implements OnInit {
  summary?: ClientSummary;
  profile?: Client;
  subscriptions: PackageSubscription[] = [];
  availablePackages: ServicePackage[] = [];
  recentRequests: ServiceRequest[] = [];
  loading = true;
  firstName = '';

  constructor(
    private dashboardService: DashboardService,
    private clientService: ClientService,
    private auth: AuthService,
    private packageService: ServicePackageService,
    private requestService: ServiceRequestService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.firstName = (this.auth.getCurrentUser()?.fullName || '').split(' ')[0];
    this.dashboardService.getClientSummary().subscribe({
      next: (s) => { this.summary = s; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.cdr.markForCheck(); },
    });
    this.clientService.getMyProfile().subscribe({
      next: (p) => { this.profile = p; this.cdr.markForCheck(); },
      error: () => {},
    });
    this.loadPackages();
  }

  private loadPackages(): void {
    this.packageService.mySubscriptions(0, 3).subscribe({
      next: (res) => {
        this.subscriptions = res.content || [];
        this.cdr.markForCheck();
      },
      error: () => {}
    });
    this.packageService.listActive().subscribe({
      next: (res) => {
        this.availablePackages = res || [];
        this.cdr.markForCheck();
      },
      error: () => {}
    });
    this.requestService.my(0, 5).subscribe({
      next: (res) => {
        this.recentRequests = res.content || [];
        this.cdr.markForCheck();
      },
      error: () => {}
    });
  }

  fmt(n: number | undefined): string {
    return n == null ? '-' : n.toLocaleString();
  }

  usagePercent(sub: PackageSubscription): number {
    if (!sub.requestQuota) return 0;
    const used = sub.requestsUsed || 0;
    return Math.min(100, Math.round((used / sub.requestQuota) * 100));
  }
}
