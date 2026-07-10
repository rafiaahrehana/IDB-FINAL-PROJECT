import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Task, TaskRequest } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly endpoint = '/tasks';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20): Observable<PagedResponse<Task>> {
    return this.api.getPaged<Task>(this.endpoint, page, size);
  }

  getById(id: number): Observable<Task> {
    return this.api.get<Task>(`${this.endpoint}/${id}`);
  }

  create(payload: TaskRequest): Observable<Task> {
    return this.api.post<Task>(this.endpoint, payload);
  }

  update(id: number, payload: TaskRequest): Observable<Task> {
    return this.api.put<Task>(`${this.endpoint}/${id}`, payload);
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
