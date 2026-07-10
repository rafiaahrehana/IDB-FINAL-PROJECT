import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Employee, CreateEmployeeRequest, UpdateEmployeeRequest, EmploymentStatus } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  // environment.apiUrl already ends with /api -> resolves to /api/employees
  private readonly endpoint = '/employees';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20, departmentId?: number, status?: EmploymentStatus): Observable<PagedResponse<Employee>> {
    const params: any = {};
    if (departmentId) params.departmentId = departmentId;
    if (status) params.status = status;
    return this.api.getPaged<Employee>(this.endpoint, page, size, params);
  }

  getById(id: number): Observable<Employee> {
    return this.api.get<Employee>(`${this.endpoint}/${id}`);
  }

  getMyProfile(): Observable<Employee> {
    return this.api.get<Employee>(`${this.endpoint}/me`);
  }

  create(payload: CreateEmployeeRequest): Observable<Employee> {
    return this.api.post<Employee>(this.endpoint, payload);
  }

  update(id: number, payload: UpdateEmployeeRequest): Observable<Employee> {
    return this.api.patch<Employee>(`${this.endpoint}/${id}`, payload);
  }

  terminate(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
