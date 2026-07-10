import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface PlanConfig {
  id: number;
  plan: string;
  displayName: string;
  description: string;
  price: number;
  currency: string;
  durationDays: number;
  featured: boolean;
  active: boolean;
  maxEmployees: number;
  maxClients: number;
  maxProjects: number;
  maxStorageMb: number;
  aiEnabled: boolean;
  websiteBuilderEnabled: boolean;
  customDomainEnabled: boolean;
  prioritySupport: boolean;
  apiAccess: boolean;
  features: string;
}

export interface CompanySubscription {
  id: number;
  companyId: number;
  companyName: string;
  companyStatus: string;
  plan: string;
  amount: number;
  currency: string;
  startDate: string;
  endDate: string;
  status: string;
  paymentMethod: string;
}

@Injectable({ providedIn: 'root' })
export class SubscriptionManagementService {
  private readonly endpoint = '/platform/subscription-plans';
  constructor(private api: ApiService) {}

  getPlans(): Observable<PlanConfig[]> {
    return this.api.get<PlanConfig[]>(this.endpoint);
  }

  getPlan(id: number): Observable<PlanConfig> {
    return this.api.get<PlanConfig>(`${this.endpoint}/${id}`);
  }

  createPlan(data: Partial<PlanConfig>): Observable<PlanConfig> {
    return this.api.post<PlanConfig>(this.endpoint, data);
  }

  updatePlan(id: number, data: Partial<PlanConfig>): Observable<PlanConfig> {
    return this.api.put<PlanConfig>(`${this.endpoint}/${id}`, data);
  }

  deletePlan(id: number): Observable<{ status: string }> {
    return this.api.delete<{ status: string }>(`${this.endpoint}/${id}`);
  }

  getCompanySubscriptions(): Observable<CompanySubscription[]> {
    return this.api.get<CompanySubscription[]>(`${this.endpoint}/company-subscriptions`);
  }

  suspendCompany(companyId: number): Observable<{ status: string }> {
    return this.api.post<{ status: string }>(`${this.endpoint}/suspend/${companyId}`, {});
  }

  activateCompany(companyId: number): Observable<{ status: string }> {
    return this.api.post<{ status: string }>(`${this.endpoint}/activate/${companyId}`, {});
  }
}
