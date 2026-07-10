import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import {
  Company,
  CompanyStatus,
  RegisterCompanyRequest,
  SubscriptionPlan,
} from '../models/platform-admin.model';

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly endpoint = '/companies';

  constructor(private api: ApiService) {}

  // LIST ALL COMPANIES (SUPER ADMIN)
  list(page = 0, size = 20, status?: CompanyStatus): Observable<PagedResponse<Company>> {
    const params: Record<string, string | number> = {};
    if (status) params['status'] = status;
    return this.api.getPaged<Company>(this.endpoint, page, size, params);
  }

  getById(id: number): Observable<Company> {
    return this.api.get<Company>(`${this.endpoint}/${id}`);
  }

  // REGISTER A NEW COMPANY WITH ITS FIRST OWNER
  register(payload: RegisterCompanyRequest): Observable<Company> {
    return this.api.post<Company>(`${this.endpoint}/admin`, payload);
  }

  // CHANGE PLAN (BACKEND EXPECTS ?plan= QUERY PARAM)
  changePlan(id: number, plan: SubscriptionPlan): Observable<Company> {
    return this.api.patch<Company>(`${this.endpoint}/${id}/plan?plan=${plan}`, {});
  }

  // CHANGE STATUS (BACKEND EXPECTS ?status= QUERY PARAM)
  changeStatus(id: number, status: CompanyStatus): Observable<Company> {
    return this.api.patch<Company>(`${this.endpoint}/${id}/status?status=${status}`, {});
  }

  deactivate(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }

  getMyCompany(): Observable<Company> {
    return this.api.get<Company>(`${this.endpoint}/me`);
  }

  deactivateMyCompany(): Observable<string> {
    return this.api.patch<string>(`${this.endpoint}/me/deactivate`, {});
  }
}
