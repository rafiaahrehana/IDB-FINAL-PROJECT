import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { AttendanceRecord, ManualAttendanceRequest } from '../models/attendance.model';

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private readonly endpoint = '/company/attendance';
  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<AttendanceRecord>> {
    return this.api.getPaged(this.endpoint, page, size); }

  getByEmployee(employeeId: number, page = 0): Observable<PagedResponse<AttendanceRecord>> {
    return this.api.getPaged(`${this.endpoint}/employee/${employeeId}`, page, 20); }

  listByStatus(status: string, page = 0): Observable<PagedResponse<AttendanceRecord>> {
    return this.api.getPaged(`${this.endpoint}/status/${status}`, page, 20); }

  checkIn(data: any): Observable<AttendanceRecord> {
    return this.api.post(`${this.endpoint}/check-in`, data); }

  checkOut(id: number, data?: any): Observable<AttendanceRecord> {
     return this.api.post(`${this.endpoint}/${id}/check-out`, data || {}); }

  approve(id: number): Observable<AttendanceRecord> {
    return this.api.patch(`${this.endpoint}/${id}/approve`, {}); }

  // HR/Admin: manually record or backdate an employee's attendance
  createManual(data: ManualAttendanceRequest): Observable<AttendanceRecord> {
    return this.api.post(`${this.endpoint}/manual`, data); }
}
