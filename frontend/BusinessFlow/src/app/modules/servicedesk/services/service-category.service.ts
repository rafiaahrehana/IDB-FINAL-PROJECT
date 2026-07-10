import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ServiceCategory, ServiceCategoryRequest } from '../models/servicedesk.model';

@Injectable({ providedIn: 'root' })
export class ServiceCategoryService {
  private readonly endpoint = '/service-categories';

  constructor(private api: ApiService) {}

  list(): Observable<ServiceCategory[]> {
    return this.api.get<ServiceCategory[]>(this.endpoint);
  }

  // Fetch all categories as a simple lookup (used for dropdowns)
  lookup(): Observable<ServiceCategory[]> {
    return this.api.get<ServiceCategory[]>(this.endpoint);
  }

  getById(id: number): Observable<ServiceCategory> {
    return this.api.get<ServiceCategory>(`${this.endpoint}/${id}`);
  }

  create(payload: ServiceCategoryRequest): Observable<ServiceCategory> {
    return this.api.post<ServiceCategory>(this.endpoint, payload);
  }

  update(id: number, payload: ServiceCategoryRequest): Observable<ServiceCategory> {
    return this.api.put<ServiceCategory>(`${this.endpoint}/${id}`, payload);
  }

  toggle(id: number): Observable<ServiceCategory> {
    return this.api.patch<ServiceCategory>(`${this.endpoint}/${id}/toggle`, {});
  }
}
