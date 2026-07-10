import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface PlanInfo {
  name: string;
  description: string;
  price: number;
  durationDays: number;
  featured: boolean;
}

export interface SubscriptionResponse {
  id: number;
  plan: string;
  amount: number;
  currency: string;
  startDate: string;
  endDate: string;
  status: string;
  paymentMethod: string;
  durationMonths: number;
  autoRenew: boolean;
}

export interface CheckoutResponse {
  success: boolean;
  gatewayPageUrl: string;
  sessionKey: string;
  tranId: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private readonly endpoint = '/company/subscription';
  constructor(private api: ApiService) {}

  getPlans(): Observable<PlanInfo[]> {
    return this.api.get<PlanInfo[]>(`${this.endpoint}/plans`);
  }

  getCurrentSubscription(): Observable<SubscriptionResponse> {
    return this.api.get<SubscriptionResponse>(`${this.endpoint}/current`);
  }

  checkout(plan: string, cusName: string, cusEmail: string): Observable<CheckoutResponse> {
    return this.api.post<CheckoutResponse>(`${this.endpoint}/checkout`, { plan, cusName, cusEmail });
  }

  activate(tranId: string): Observable<{ status: string; tranId: string }> {
    return this.api.post<{ status: string; tranId: string }>(`${this.endpoint}/activate/${tranId}`, {});
  }
}
