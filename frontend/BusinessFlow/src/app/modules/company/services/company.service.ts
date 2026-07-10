import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface CompanyResponse {
  id: number;
  companyName: string;
  subdomain: string;
  status: string;
  subscriptionPlan: string;
  active: boolean;
  subscriptionStart?: string;
  subscriptionEnd?: string;
}

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly endpoint = '/companies';
  constructor(private api: ApiService) {}

  getMyCompany(): Observable<CompanyResponse> {
    return this.api.get<CompanyResponse>(`${this.endpoint}/me`);
  }

  deactivateMyCompany(): Observable<string> {
    return this.api.patch<string>(`${this.endpoint}/me/deactivate`, {});
  }

  deleteMyCompany(): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/me`);
  }
}
