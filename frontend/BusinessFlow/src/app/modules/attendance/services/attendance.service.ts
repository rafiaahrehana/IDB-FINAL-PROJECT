import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { AttendanceRecord } from '../models/attendance.model';

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private readonly endpoint = '/company/attendance';
  constructor(private api: ApiService) {}
  // Backend has no bare "list all" endpoint; use the company-wide date-range
  // report with a wide default range to populate the unfiltered list view.
  list(page = 0, size = 20): Observable<PagedResponse<AttendanceRecord>> {
    const fmt = (d: Date) => d.toISOString().slice(0, 10);
    const now = new Date();
    const start = new Date(now.getFullYear(), 0, 1);
    return this.api.getPaged(`${this.endpoint}/date-range`, page, size, {
      startDate: fmt(start),
      endDate: fmt(now),
    });
  }
  getByEmployee(employeeId: number, page = 0): Observable<PagedResponse<AttendanceRecord>> { return this.api.getPaged(`${this.endpoint}/employee/${employeeId}`, page, 20); }
  listByStatus(status: string, page = 0): Observable<PagedResponse<AttendanceRecord>> { return this.api.getPaged(`${this.endpoint}/status/${status}`, page, 20); }
  checkIn(data: any): Observable<AttendanceRecord> { return this.api.post(`${this.endpoint}/check-in`, data); }
  checkOut(id: number, data?: any): Observable<AttendanceRecord> { return this.api.post(`${this.endpoint}/${id}/check-out`, data || {}); }
  approve(id: number): Observable<AttendanceRecord> { return this.api.patch(`${this.endpoint}/${id}/approve`, {}); }
}
