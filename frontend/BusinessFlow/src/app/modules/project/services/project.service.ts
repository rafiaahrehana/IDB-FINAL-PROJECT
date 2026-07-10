import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Project, ProjectRequest } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly endpoint = '/projects';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<Project>> {
    return this.api.getPaged<Project>(this.endpoint, page, size);
  }

  getById(id: number): Observable<Project> {
    return this.api.get<Project>(`${this.endpoint}/${id}`);
  }

  create(payload: ProjectRequest): Observable<Project> {
    return this.api.post<Project>(this.endpoint, payload);
  }

  update(id: number, payload: ProjectRequest): Observable<Project> {
    return this.api.put<Project>(`${this.endpoint}/${id}`, payload);
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
