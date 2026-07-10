import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { OfferLetter, OfferLetterRequest } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class OfferLetterService {
  private readonly endpoint = '/hr/letters';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<OfferLetter>> {
    return this.api.getPaged<OfferLetter>(this.endpoint, page, size);
  }

  listForEmployee(employeeId: number, page = 0, size = 20): Observable<PagedResponse<OfferLetter>> {
    return this.api.getPaged<OfferLetter>(`${this.endpoint}/employee/${employeeId}`, page, size);
  }

  getById(id: number): Observable<OfferLetter> {
    return this.api.get<OfferLetter>(`${this.endpoint}/${id}`);
  }

  create(payload: OfferLetterRequest): Observable<OfferLetter> {
    return this.api.post<OfferLetter>(this.endpoint, payload);
  }

  issue(id: number): Observable<OfferLetter> {
    return this.api.patch<OfferLetter>(`${this.endpoint}/${id}/issue`, {});
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
