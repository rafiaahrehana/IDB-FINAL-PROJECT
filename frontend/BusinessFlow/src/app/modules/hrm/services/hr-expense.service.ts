import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { HrExpense, HrExpenseRequest } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class HrExpenseService {
  private readonly endpoint = '/company/finance/expenses';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<HrExpense>> {
    return this.api.getPaged<HrExpense>(this.endpoint, page, size);
  }

  listMine(page = 0, size = 20): Observable<PagedResponse<HrExpense>> {
    return this.api.getPaged<HrExpense>(`${this.endpoint}/my-expenses`, page, size);
  }

  getById(id: number): Observable<HrExpense> {
    return this.api.get<HrExpense>(`${this.endpoint}/${id}`);
  }

  submit(payload: HrExpenseRequest): Observable<HrExpense> {
    return this.api.post<HrExpense>(this.endpoint, payload);
  }

  approve(id: number, notes = ''): Observable<void> {
    return this.api.post<void>(`${this.endpoint}/${id}/approve?notes=${encodeURIComponent(notes)}`, {});
  }

  reject(id: number, reason: string): Observable<void> {
    return this.api.post<void>(`${this.endpoint}/${id}/reject?reason=${encodeURIComponent(reason)}`, {});
  }

  markAsPaid(id: number, reimbursementMethod?: string, referenceNumber?: string): Observable<void> {
    let url = `${this.endpoint}/${id}/mark-as-paid`;
    const params: string[] = [];
    if (reimbursementMethod) params.push(`reimbursementMethod=${encodeURIComponent(reimbursementMethod)}`);
    if (referenceNumber) params.push(`referenceNumber=${encodeURIComponent(referenceNumber)}`);
    if (params.length) url += '?' + params.join('&');
    return this.api.post<void>(url, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }
}
