import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { SoftwareLicense } from '../models/itam.model';

@Injectable({ providedIn: 'root' })
export class SoftwareService {
  private readonly endpoint = '/v1/itam/software';
  constructor(private api: ApiService) {}
  list(page = 0, size = 20): Observable<PagedResponse<SoftwareLicense>> { return this.api.getPaged(this.endpoint, page, size); }
  listByStatus(status: string, page = 0): Observable<PagedResponse<SoftwareLicense>> { return this.api.getPaged(`${this.endpoint}/status/${status}`, page, 20); }
  expiring(): Observable<PagedResponse<SoftwareLicense>> { return this.api.getPaged(`${this.endpoint}/expiring`, 0, 50); }
  getById(id: number): Observable<SoftwareLicense> { return this.api.get(`${this.endpoint}/${id}`); }
  create(payload: any): Observable<SoftwareLicense> { return this.api.post(this.endpoint, payload); }
  update(id: number, payload: any): Observable<SoftwareLicense> { return this.api.patch(`${this.endpoint}/${id}`, payload); }
  delete(id: number): Observable<void> { return this.api.delete(`${this.endpoint}/${id}`); }
}
