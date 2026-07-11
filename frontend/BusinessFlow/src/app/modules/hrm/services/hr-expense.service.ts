import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { HrExpense, HrExpenseRequest } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class HrExpenseService {
  private readonly endpoint = '/hr/expenses';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<HrExpense>> {
    return this.api.getPaged<HrExpense>(this.endpoint, page, size);
  }

  listMine(page = 0, size = 20): Observable<PagedResponse<HrExpense>> {
    return this.api.getPaged<HrExpense>(`${this.endpoint}/my`, page, size);
  }

  getById(id: number): Observable<HrExpense> {
    return this.api.get<HrExpense>(`${this.endpoint}/${id}`);
  }

  submit(payload: HrExpenseRequest): Observable<HrExpense> {
    return this.api.post<HrExpense>(this.endpoint, payload);
  }

  approve(id: number): Observable<HrExpense> {
    return this.api.patch<HrExpense>(`${this.endpoint}/${id}/approve`, {});
  }

  reject(id: number, rejectionReason: string): Observable<HrExpense> {
    return this.api.patch<HrExpense>(`${this.endpoint}/${id}/reject`, { rejectionReason });
  }

  reimburse(id: number): Observable<HrExpense> {
    return this.api.patch<HrExpense>(`${this.endpoint}/${id}/reimburse`, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }
}
