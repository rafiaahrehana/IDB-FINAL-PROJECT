import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { SalaryStructure, SalaryStructureRequest } from '../models/hrm.model';

@Injectable({ providedIn: 'root' })
export class SalaryStructureService {
  private readonly endpoint = '/hr/salary-structures';

  constructor(private api: ApiService) {}

  getById(id: number): Observable<SalaryStructure> {
    return this.api.get<SalaryStructure>(`${this.endpoint}/${id}`);
  }

  getActive(employeeId: number): Observable<SalaryStructure> {
    return this.api.get<SalaryStructure>(`${this.endpoint}/employee/${employeeId}/active`);
  }

  listForEmployee(employeeId: number, page = 0, size = 10): Observable<PagedResponse<SalaryStructure>> {
    return this.api.getPaged<SalaryStructure>(`${this.endpoint}/employee/${employeeId}`, page, size);
  }

  history(employeeId: number): Observable<SalaryStructure[]> {
    return this.api.get<SalaryStructure[]>(`${this.endpoint}/employee/${employeeId}/history`);
  }

  create(payload: SalaryStructureRequest): Observable<SalaryStructure> {
    return this.api.post<SalaryStructure>(this.endpoint, payload);
  }

  delete(id: number): Observable<string> {
    return this.api.delete<string>(`${this.endpoint}/${id}`);
  }
}
