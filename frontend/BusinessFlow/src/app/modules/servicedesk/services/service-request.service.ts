import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from '../../../core/services/api.service';
import { RequestComment, ServiceRequest } from '../models/servicedesk.model';

export interface CreateServiceRequestRequest {
  title: string;
  description?: string;
  hubServiceId: number;
  priority?: string;
  agreedPrice?: number;
  slaDeadline?: string;
  subscriptionId?: number;
  paymentChoice: string;
  paymentMethod?: string;
  // Answers to the service's dynamic form fields, keyed by ServiceFormField id
  formData?: Record<string, string>;
}

export interface UpdateServiceRequestRequest {
  title?: string;
  description?: string;
  priority?: string;
  agreedPrice?: number;
  slaDeadline?: string;
  assignedEmployeeId?: number;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: string;
  dueDate?: string;
  slaDeadline?: string;
  assignedEmployeeId?: number;
  workflowStageId?: number;
}

export interface SubmitQuotationRequest {
  amount: number;
  currency?: string;
  notes?: string;
  validUntil?: string;
}

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  private readonly endpoint = '/service-requests';

  constructor(private api: ApiService) {}

  create(payload: CreateServiceRequestRequest): Observable<ServiceRequest> {
    return this.api.post<ServiceRequest>(this.endpoint, payload);
  }

  list(page = 0, size = 20, params?: any): Observable<PagedResponse<ServiceRequest>> {
    return this.api.getPaged<ServiceRequest>(this.endpoint, page, size, params);
  }

  my(page = 0, size = 20): Observable<PagedResponse<ServiceRequest>> {
    return this.api.getPaged<ServiceRequest>(`${this.endpoint}/my`, page, size);
  }

  assignedToMe(page = 0, size = 20): Observable<PagedResponse<ServiceRequest>> {
    return this.api.getPaged<ServiceRequest>(`${this.endpoint}/assigned-to-me`, page, size);
  }

  getById(id: number): Observable<ServiceRequest> {
    return this.api.get<ServiceRequest>(`${this.endpoint}/${id}`);
  }

  update(id: number, payload: UpdateServiceRequestRequest): Observable<ServiceRequest> {
    return this.api.patch<ServiceRequest>(`${this.endpoint}/${id}`, payload);
  }

  changeStatus(id: number, status: string, reason?: string): Observable<ServiceRequest> {
    return this.api.patch<ServiceRequest>(`${this.endpoint}/${id}/status`, { status, reason });
  }

  assign(id: number, employeeId: number): Observable<ServiceRequest> {
    return this.api.patch<ServiceRequest>(`${this.endpoint}/${id}/assign/${employeeId}`, {});
  }

  cancel(id: number): Observable<string> {
    return this.api.patchText(`${this.endpoint}/${id}/cancel`, {});
  }

  // TASKS
  addTask(id: number, payload: CreateTaskRequest): Observable<any> {
    return this.api.post<any>(`${this.endpoint}/${id}/tasks`, payload);
  }

  getTasks(id: number): Observable<any[]> {
    return this.api.get<any[]>(`${this.endpoint}/${id}/tasks`);
  }

  updateTask(id: number, taskId: number, payload: Partial<CreateTaskRequest> & { status?: string }): Observable<any> {
    return this.api.patch<any>(`${this.endpoint}/${id}/tasks/${taskId}`, payload);
  }

  deleteTask(id: number, taskId: number): Observable<string> {
    return this.api.deleteText(`${this.endpoint}/${id}/tasks/${taskId}`);
  }

  // COMMENTS - backend returns Page<RequestCommentResponse>, not a plain array
  comments(id: number, page = 0, size = 20): Observable<PagedResponse<RequestComment>> {
    return this.api.getPaged<RequestComment>(`${this.endpoint}/${id}/comments`, page, size);
  }

  addComment(id: number, content: string): Observable<RequestComment> {
    return this.api.post<RequestComment>(`${this.endpoint}/${id}/comments`, { content });
  }

  history(id: number): Observable<any[]> {
    return this.api.get<any[]>(`${this.endpoint}/${id}/history`);
  }

  // QUOTATION - nested on the service request itself; there is no standalone quotation list/CRUD endpoint
  submitQuotation(id: number, payload: SubmitQuotationRequest): Observable<ServiceRequest> {
    return this.api.post<ServiceRequest>(`${this.endpoint}/${id}/quotation`, payload);
  }

  acceptQuotation(id: number): Observable<ServiceRequest> {
    return this.api.post<ServiceRequest>(`${this.endpoint}/${id}/quotation/accept`, {});
  }

  rejectQuotation(id: number, reason?: string): Observable<ServiceRequest> {
    return this.api.post<ServiceRequest>(`${this.endpoint}/${id}/quotation/reject`, { reason });
  }
}
