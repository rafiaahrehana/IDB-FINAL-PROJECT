import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { Opportunity, OpportunityStage, PipelineSummary } from '../models/crm.model';

@Injectable({ providedIn: 'root' })
export class OpportunityService {
  private readonly endpoint = '/crm/opportunities';

  constructor(private api: ApiService) {}

  list(page = 0, size = 20, params?: any): Observable<PagedResponse<Opportunity>> {
    return this.api.getPaged<Opportunity>(this.endpoint, page, size, params);
  }

  getById(id: number): Observable<Opportunity> {
    return this.api.get<Opportunity>(`${this.endpoint}/${id}`);
  }

  create(payload: Partial<Opportunity>): Observable<Opportunity> {
    return this.api.post<Opportunity>(this.endpoint, payload);
  }

  createFromLead(leadId: number, payload: Partial<Opportunity>): Observable<Opportunity> {
    return this.api.post<Opportunity>(`${this.endpoint}/from-lead/${leadId}`, payload);
  }

  update(id: number, payload: Partial<Opportunity>): Observable<Opportunity> {
    return this.api.patch<Opportunity>(`${this.endpoint}/${id}`, payload);
  }

  changeStage(id: number, stage: OpportunityStage, lostReason?: string): Observable<Opportunity> {
    return this.api.patch<Opportunity>(`${this.endpoint}/${id}/stage`, { stage, lostReason });
  }

  pipelineSummary(): Observable<PipelineSummary> {
    return this.api.get<PipelineSummary>(`${this.endpoint}/pipeline-summary`);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }
}
