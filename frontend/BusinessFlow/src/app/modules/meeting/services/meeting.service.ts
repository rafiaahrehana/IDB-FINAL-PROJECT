import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Meeting, MeetingRequest } from '../models/meeting.model';

@Injectable({ providedIn: 'root' })
export class MeetingService {
  private readonly endpoint = '/meetings';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<Meeting>> {
    return this.api.getPaged<Meeting>(this.endpoint, page, size);
  }

  getById(id: number): Observable<Meeting> {
    return this.api.get<Meeting>(`${this.endpoint}/${id}`);
  }

  create(payload: MeetingRequest): Observable<Meeting> {
    return this.api.post<Meeting>(this.endpoint, payload);
  }

  update(id: number, payload: MeetingRequest): Observable<Meeting> {
    return this.api.put<Meeting>(`${this.endpoint}/${id}`, payload);
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
