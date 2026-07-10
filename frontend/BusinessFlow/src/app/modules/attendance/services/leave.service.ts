import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { AttendanceLeave } from '../models/attendance.model';

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private readonly endpoint = '/hr/leaves';
  constructor(private api: ApiService) {}
  list(page = 0): Observable<PagedResponse<AttendanceLeave>> { return this.api.getPaged(this.endpoint, page, 20); }
  getById(id: number): Observable<AttendanceLeave> { return this.api.get(`${this.endpoint}/${id}`); }
  create(payload: any): Observable<AttendanceLeave> { return this.api.post(this.endpoint, payload); }
  // Backend only has a single PATCH /{id}/review endpoint (ReviewLeaveRequest body
  // with a status field) - there is no separate /approve or /reject path.
  approve(id: number): Observable<AttendanceLeave> {
    return this.api.patch(`${this.endpoint}/${id}/review`, { status: 'APPROVED' });
  }
  reject(id: number, reason: string): Observable<AttendanceLeave> {
    return this.api.patch(`${this.endpoint}/${id}/review`, { status: 'REJECTED', rejectionReason: reason });
  }
}
