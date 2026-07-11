import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Invoice } from '../models/finance.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly endpoint = '/company/finance/invoices';
  constructor(private api: ApiService) {}
  list(page = 0, size = 20): Observable<PagedResponse<Invoice>> {
    return this.api.getPaged<Invoice>(this.endpoint, page, size);
  }
  listByStatus(status: string, page = 0): Observable<PagedResponse<Invoice>> {
    return this.api.getPaged<Invoice>(`${this.endpoint}/status/${status}`, page, 20);
  }
  overdue(page = 0): Observable<PagedResponse<Invoice>> {
    return this.api.getPaged<Invoice>(`${this.endpoint}/overdue`, page, 20);
  }
  getById(id: number): Observable<Invoice> {
    return this.api.get<Invoice>(`${this.endpoint}/${id}`);
  }
  create(payload: any): Observable<Invoice> {
    return this.api.post<Invoice>(this.endpoint, payload);
  }
  send(id: number): Observable<Invoice> {
    return this.api.post<Invoice>(`${this.endpoint}/${id}/send`, {});
  }
  markPaid(id: number): Observable<Invoice> {
    return this.api.post<Invoice>(`${this.endpoint}/${id}/mark-as-paid`, {});
  }
  recordPayment(id: number, amount: number, method: string): Observable<any> {
    return this.api.post<any>(`${this.endpoint}/${id}/record-payment`, { amount, method });
  }
}
