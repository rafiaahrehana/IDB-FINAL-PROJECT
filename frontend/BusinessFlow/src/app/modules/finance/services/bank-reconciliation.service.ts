import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { BankReconciliation, BankReconciliationRequest } from '../models/finance.model';

@Injectable({ providedIn: 'root' })
export class BankReconciliationService {
  private readonly endpoint = '/company/finance/reconciliation';
  constructor(private api: ApiService) {}

  create(payload: BankReconciliationRequest): Observable<BankReconciliation> {
    return this.api.post<BankReconciliation>(this.endpoint, payload);
  }

  getById(id: number): Observable<BankReconciliation> {
    return this.api.get<BankReconciliation>(`${this.endpoint}/${id}`);
  }

  list(page = 0, size = 20): Observable<PagedResponse<BankReconciliation>> {
    return this.api.getPaged<BankReconciliation>(this.endpoint, page, size);
  }

  // Backend expects "notes" as an optional @RequestParam query param, not a JSON body
  markAsReconciled(id: number, notes?: string): Observable<void> {
    return this.api.post<void>(`${this.endpoint}/${id}/reconcile${notes ? '?notes=' + encodeURIComponent(notes) : ''}`, {});
  }

  getPending(): Observable<BankReconciliation[]> {
    return this.api.get<BankReconciliation[]>(`${this.endpoint}/pending`);
  }
}
