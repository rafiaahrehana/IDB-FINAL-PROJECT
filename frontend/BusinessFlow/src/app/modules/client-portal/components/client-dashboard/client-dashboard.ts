import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService, ClientSummary } from '../../../../core/services/dashboard.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../crm/services/client.service';
import { Client } from '../../../crm/models/crm.model';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-client-dashboard',
  imports: [CommonModule, RouterLink, Loader],
  templateUrl: './client-dashboard.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientDashboard implements OnInit {
  summary?: ClientSummary;
  profile?: Client;
  loading = true;
  firstName = '';

  constructor(
    private dashboardService: DashboardService,
    private clientService: ClientService,
    private auth: AuthService,
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
  }

  fmt(n: number | undefined): string {
    return n == null ? '-' : n.toLocaleString();
  }
}
