import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Lead } from '../models/crm.model';

@Injectable({ providedIn: 'root' })
export class LeadService {
  private readonly endpoint = '/crm/leads';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20, params?: any): Observable<PagedResponse<Lead>> {
    return this.api.getPaged<Lead>(this.endpoint, page, size, params);
  }

  getById(id: number): Observable<Lead> {
    return this.api.get<Lead>(`${this.endpoint}/${id}`);
  }

  create(payload: Partial<Lead>): Observable<Lead> {
    return this.api.post<Lead>(this.endpoint, payload);
  }

  // Backend only accepts keyword on this dedicated endpoint - GET /crm/leads ignores
  // an unknown "keyword" param silently, so this must be a separate call.
  search(keyword: string, page = 0, size = 20): Observable<PagedResponse<Lead>> {
    return this.api.getPaged<Lead>(`${this.endpoint}/search`, page, size, { keyword });
  }

  convert(id: number): Observable<Lead> {
    return this.api.patch<Lead>(`${this.endpoint}/${id}/convert`, {});
  }

  update(id: number, payload: Partial<Lead>): Observable<Lead> {
    return this.api.patch<Lead>(`${this.endpoint}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }
}
