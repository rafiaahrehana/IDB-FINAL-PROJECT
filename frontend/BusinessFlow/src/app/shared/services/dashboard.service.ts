import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface DashboardSummary {
  totalLeads: number;
  newLeads: number;
  qualifiedLeads: number;
  totalClients: number;
  openOpportunities: number;
  pipelineValue: number;
  weightedForecast: number;
  pendingRequests: number;
  inProgressRequests: number;
  completedRequestsAllTime: number;
  slaBreachedOpen: number;
  openTickets: number;
  newTickets: number;
  outstandingInvoiceAmount: number;
  walletBalance: number;
  walletCreditBalance: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private api: ApiService) {}

  getSummary(): Observable<DashboardSummary> {
    return this.api.get<DashboardSummary>('/dashboard/summary');
  }
}
