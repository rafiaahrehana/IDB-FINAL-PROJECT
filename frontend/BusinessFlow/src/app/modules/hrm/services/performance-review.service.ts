import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { PerformanceReview, PerformanceReviewRequest } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class PerformanceReviewService {
  private readonly endpoint = '/hr/performance';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<PerformanceReview>> {
    return this.api.getPaged<PerformanceReview>(this.endpoint, page, size);
  }

  listForEmployee(employeeId: number, page = 0, size = 20): Observable<PagedResponse<PerformanceReview>> {
    return this.api.getPaged<PerformanceReview>(`${this.endpoint}/employee/${employeeId}`, page, size);
  }

  getById(id: number): Observable<PerformanceReview> {
    return this.api.get<PerformanceReview>(`${this.endpoint}/${id}`);
  }

  create(payload: PerformanceReviewRequest): Observable<PerformanceReview> {
    return this.api.post<PerformanceReview>(this.endpoint, payload);
  }

  update(id: number, payload: PerformanceReviewRequest): Observable<PerformanceReview> {
    return this.api.patch<PerformanceReview>(`${this.endpoint}/${id}`, payload);
  }

  finalise(id: number): Observable<PerformanceReview> {
    return this.api.patch<PerformanceReview>(`${this.endpoint}/${id}/finalise`, {});
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
