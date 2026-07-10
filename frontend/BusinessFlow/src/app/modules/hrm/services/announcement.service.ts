import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Announcement, AnnouncementRequest } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class AnnouncementService {
  private readonly endpoint = '/announcements';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<Announcement>> {
    return this.api.getPaged<Announcement>(this.endpoint, page, size);
  }

  listActive(): Observable<Announcement[]> {
    return this.api.get<Announcement[]>(`${this.endpoint}/active`);
  }

  getById(id: number): Observable<Announcement> {
    return this.api.get<Announcement>(`${this.endpoint}/${id}`);
  }

  create(payload: AnnouncementRequest): Observable<Announcement> {
    return this.api.post<Announcement>(this.endpoint, payload);
  }

  update(id: number, payload: AnnouncementRequest): Observable<Announcement> {
    return this.api.put<Announcement>(`${this.endpoint}/${id}`, payload);
  }

  publish(id: number): Observable<Announcement> {
    return this.api.patch<Announcement>(`${this.endpoint}/${id}/publish`, {});
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
