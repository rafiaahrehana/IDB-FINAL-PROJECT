import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RequestComment, ServiceRequest, StageApproval } from '../../models/servicedesk.model';
import { ServiceRequestService } from '../../services/service-request.service';
import { ServiceFormFieldService } from '../../services/service-form-field.service';
import { ApprovalService } from '../../services/approval.service';
import { AuthService } from '../../../../core/services/auth.service';
import { EmployeeService } from '../../../hrm/services/employee.service';
import { Employee } from '../../../hrm/models/hrm.model';

// Mirror of backend ServiceRequestStatus / TaskStatus enums
const REQUEST_STATUSES = ['PENDING', 'QUOTATION_PENDING', 'ASSIGNED', 'IN_PROGRESS',
  'WAITING_CLIENT', 'UNDER_REVIEW', 'COMPLETED', 'REJECTED', 'CANCELLED', 'RESUBMITTED'] as const;
const TASK_STATUSES = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CANCELLED'] as const;

@Component({
  selector: 'app-request-detail',
  imports: [CommonModule, FormsModule],
  templateUrl: './request-detail.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './request-detail.scss',
})
export class RequestDetail implements OnInit {
  requestId!: number;
  request?: ServiceRequest;
  approvals: StageApproval[] = [];
  comments: RequestComment[] = [];
  history: any[] = [];
  newComment = '';

  // Client's dynamic form answers with labels resolved from the field definitions
  formAnswers: { label: string; value: string }[] = [];
  error = '';
  info = '';

  // Staff manage the lifecycle; clients can cancel their own request
  isStaff = false;
  isClient = false;
  // Quotation accept/reject is CLIENT or COMPANY_OWNER (owner acting on the client's behalf)
  canDecideQuotation = false;

  // Status / assignment (staff)
  requestStatuses = REQUEST_STATUSES;
  newStatus = '';
  statusReason = '';
  employees: Employee[] = [];
  assignEmployeeId: number | null = null;

  // Tasks (staff)
  tasks: any[] = [];
  taskStatuses = TASK_STATUSES;
  newTask: any = { title: '' };
  showTaskForm = false;

  // Quotation (embedded on the service request - there is no standalone Quotation endpoint)
  quotationForm = { amount: 0, currency: 'USD', notes: '', validUntil: '' };
  rejectReason = '';

  constructor(
    private route: ActivatedRoute,
    private requestService: ServiceRequestService,
    private approvalService: ApprovalService,
    private formFieldService: ServiceFormFieldService,
    private auth: AuthService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  // Answers are stored keyed by field id; resolve labels from the service's
  // field definitions (fields deleted since submission fall back to "Field {id}")
  private resolveFormAnswers(r: ServiceRequest): void {
    this.formAnswers = [];
    if (!r.formData || !Object.keys(r.formData).length || !r.hubServiceId) return;
    this.formFieldService.list(r.hubServiceId).subscribe({
      next: (fields) => {
        const labels = new Map(fields.map((f) => [String(f.id), f.label]));
        this.formAnswers = Object.entries(r.formData!).map(([id, value]) => ({
          label: labels.get(id) || `Field ${id}`,
          value,
        }));
        this.cdr.markForCheck();
      },
      error: () => {
        this.formAnswers = Object.entries(r.formData!).map(([id, value]) => ({
          label: `Field ${id}`,
          value,
        }));
        this.cdr.markForCheck();
      },
    });
  }

  ngOnInit(): void {
    this.requestId = Number(this.route.snapshot.paramMap.get('id'));
    this.isStaff = this.auth.hasAnyRole(['COMPANY_OWNER', 'EMPLOYEE']);
    this.isClient = this.auth.hasRole('CLIENT');
    this.canDecideQuotation = this.isClient || this.auth.hasRole('COMPANY_OWNER');
    this.loadAll();
    if (this.isStaff) {
      this.employeeService.list(0, 100).subscribe({ next: (res) => {
        this.employees = res.content;
        this.cdr.markForCheck();
      } });
    }
  }

  loadAll(): void {
    this.requestService.getById(this.requestId).subscribe({
      next: (r) => {
        this.request = r;
        this.newStatus = r.status;
        this.resolveFormAnswers(r);
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load request';
        this.cdr.markForCheck();
      },
    });
    this.approvalService
      .forRequest(this.requestId)
      .subscribe({ next: (a) => {
        this.approvals = a;
        this.cdr.markForCheck();
      } });
    this.requestService.comments(this.requestId).subscribe({ next: (c) => {
      this.comments = c.content;
      this.cdr.markForCheck();
    } });
    if (this.isStaff) {
      this.requestService.history(this.requestId).subscribe({ next: (h) => {
        this.history = h;
        this.cdr.markForCheck();
      } });
      this.loadTasks();
    }
  }

  loadTasks(): void {
    this.requestService.getTasks(this.requestId).subscribe({ next: (t) => {
      this.tasks = t;
      this.cdr.markForCheck();
    } });
  }

  // ----- Lifecycle (staff) -----

  changeStatus(): void {
    if (!this.request || !this.newStatus || this.newStatus === this.request.status) return;
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.changeStatus(this.requestId, this.newStatus, this.statusReason || undefined).subscribe({
      next: (r) => {
        this.request = r;
        this.statusReason = '';
        this.info = 'Status updated';
        this.loadAll();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to change status';
        this.cdr.markForCheck();
      },
    });
  }

  assign(): void {
    if (!this.assignEmployeeId) return;
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.assign(this.requestId, this.assignEmployeeId).subscribe({
      next: (r) => {
        this.request = r;
        this.assignEmployeeId = null;
        this.info = 'Request assigned';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to assign';
        this.cdr.markForCheck();
      },
    });
  }

  // ----- Cancel (client or staff) -----

  cancel(): void {
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.cancel(this.requestId).subscribe({
      next: () => {
        this.info = 'Request cancelled';
        this.loadAll();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to cancel request';
        this.cdr.markForCheck();
      },
    });
  }

  // ----- Tasks (staff) -----

  addTask(): void {
    if (!this.newTask.title?.trim()) return;
    this.requestService.addTask(this.requestId, this.newTask).subscribe({
      next: () => {
        this.newTask = { title: '' };
        this.showTaskForm = false;
        this.loadTasks();
        this.refreshRequestOnly();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to create task';
        this.cdr.markForCheck();
      },
    });
  }

  setTaskStatus(task: any, status: string): void {
    if (status === task.status) return;
    this.requestService.updateTask(this.requestId, task.id, { status }).subscribe({
      next: () => {
        this.loadTasks();
        this.refreshRequestOnly();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update task';
        this.cdr.markForCheck();
      },
    });
  }

  deleteTask(task: any): void {
    this.requestService.deleteTask(this.requestId, task.id).subscribe({
      next: () => {
        this.loadTasks();
        this.refreshRequestOnly();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete task';
        this.cdr.markForCheck();
      },
    });
  }

  private refreshRequestOnly(): void {
    this.requestService.getById(this.requestId).subscribe({ next: (r) => {
      this.request = r;
      this.cdr.markForCheck();
    } });
  }

  addComment(): void {
    if (!this.newComment.trim()) return;
    this.requestService.addComment(this.requestId, this.newComment.trim()).subscribe({
      next: () => {
        this.newComment = '';
        this.loadAll();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to add comment';
        this.cdr.markForCheck();
      },
    });
  }

  submitQuotation(): void {
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.submitQuotation(this.requestId, this.quotationForm).subscribe({
      next: (r) => {
        this.request = r;
        this.info = 'Quotation submitted';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to submit quotation';
        this.cdr.markForCheck();
      },
    });
  }

  acceptQuotation(): void {
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.acceptQuotation(this.requestId).subscribe({
      next: (r) => {
        this.request = r;
        this.info = 'Quotation accepted';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to accept quotation';
        this.cdr.markForCheck();
      },
    });
  }

  rejectQuotation(): void {
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.rejectQuotation(this.requestId, this.rejectReason || undefined).subscribe({
      next: (r) => {
        this.request = r;
        this.rejectReason = '';
        this.info = 'Quotation rejected';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to reject quotation';
        this.cdr.markForCheck();
      },
    });
  }

  hasPendingApproval(): boolean {
    return this.approvals.some((a) => a.status === 'PENDING');
  }
}
