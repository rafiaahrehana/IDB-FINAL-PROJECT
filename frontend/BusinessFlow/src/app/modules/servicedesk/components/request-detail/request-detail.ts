import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RequestComment, ServiceRequest, StageApproval } from '../../models/servicedesk.model';
import { ServiceRequestService, RequestDocument } from '../../services/service-request.service';
import { ServiceFormFieldService } from '../../services/service-form-field.service';
import { ApprovalService } from '../../services/approval.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ChatSocketService } from '../../../../core/services/chat-socket.service';
import { ChatThread, ChatMessage } from '../../../../shared/components/chat-thread/chat-thread';
import { EmployeeService } from '../../../hrm/services/employee.service';
import { Employee } from '../../../hrm/models/hrm.model';
import { ApiService } from '../../../../core/services/api.service';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

// Mirror of backend ServiceRequestStatus / TaskStatus enums
const REQUEST_STATUSES = ['PENDING', 'QUOTATION_PENDING', 'ASSIGNED', 'IN_PROGRESS',
  'WAITING_CLIENT', 'UNDER_REVIEW', 'COMPLETED', 'REJECTED', 'CANCELLED', 'RESUBMITTED'] as const;
const TASK_STATUSES = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CANCELLED'] as const;

@Component({
  selector: 'app-request-detail',
  imports: [CommonModule, FormsModule, HasPermissionDirective, ChatThread],
  templateUrl: './request-detail.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './request-detail.scss',
})
export class RequestDetail implements OnInit, OnDestroy {
  requestId!: number;
  request?: ServiceRequest;
  approvals: StageApproval[] = [];
  comments: RequestComment[] = [];
  history: any[] = [];
  posting = false;

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
  // Once a request has an assignee, show it as read-only text instead of an
  // always-open dropdown; this flips to true to reveal the dropdown for reassigning.
  editingAssignment = false;

  // Tasks (staff)
  tasks: any[] = [];
  taskStatuses = TASK_STATUSES;
  newTask: any = { title: '', assignedEmployeeId: null, dueDate: '', priority: 'NORMAL', estimatedHours: null, description: '' };
  showTaskForm = false;

  // Quotation (embedded on the service request - there is no standalone Quotation endpoint)
  quotationForm = { amount: 0, currency: 'BDT', notes: '', validUntil: '' };
  rejectReason = '';

  // Documents (client or staff can attach files - budget docs, requirements, deliverables)
  documents: RequestDocument[] = [];
  uploading = false;
  documentLabel = '';

  summarising = false;
  summaryError = '';

  constructor(
    private route: ActivatedRoute,
    private requestService: ServiceRequestService,
    private approvalService: ApprovalService,
    private formFieldService: ServiceFormFieldService,
    private auth: AuthService,
    private employeeService: EmployeeService,
    private api: ApiService,
    private cdr: ChangeDetectorRef,
    private chatSocket: ChatSocketService,
  ) {}

  private chatUnsubscribe?: () => void;

  get chatConnected(): boolean {
    return this.chatSocket.connected;
  }

  // ChatThread wants oldest-first bubbles; the API returns newest-first
  // (findByServiceRequestIdOrderByCreatedAtDesc), and INTERNAL comments (staff
  // notes clients never see, per getComments()'s visibility filter) get the
  // "Internal note" pill so staff can tell them apart from the client-facing thread.
  //
  // This used to be a getter that remapped `comments` on every template read, which
  // handed ChatThread a brand-new array on every change-detection tick (e.g. every
  // keystroke anywhere on the page) - ChatThread treated that as "new messages
  // arrived" and auto-scrolled itself into view, yanking the whole page down while
  // someone was mid-sentence in the composer. Caching it as a plain property that
  // only updates when `comments` actually changes fixes that.
  chatMessages: ChatMessage[] = [];

  private syncChatMessages(): void {
    this.chatMessages = [...this.comments].reverse().map((c) => ({
      id: c.id,
      authorId: c.authorId ?? 0,
      authorName: c.authorName || 'Unknown',
      content: c.content,
      createdAt: c.createdAt,
      internal: c.visibility === 'INTERNAL',
    }));
  }

  get currentUserId(): number | null {
    return this.auth.getCurrentUser()?.id ?? null;
  }

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

  isUrl(value: string): boolean {
    if (!value || typeof value !== 'string') return false;
    return value.startsWith('http://') || value.startsWith('https://');
  }

  isImageUrl(value: string): boolean {
    if (!this.isUrl(value)) return false;
    const lower = value.toLowerCase();
    return lower.includes('.png') || lower.includes('.jpg') || lower.includes('.jpeg') || 
           lower.includes('.gif') || lower.includes('.webp') || lower.includes('.svg') || 
           lower.includes('cloudinary');
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

    this.chatUnsubscribe = this.chatSocket.subscribe(
      `/user/queue/service-requests/${this.requestId}/messages`,
      (message: RequestComment) => {
        // Comments load newest-first (findByServiceRequestIdOrderByCreatedAtDesc) -
        // a live one belongs at the top for the same reason. Guard against a
        // duplicate if this tab is also the sender and loadAll() already refetched.
        if (this.comments.some((c) => c.id === message.id)) return;
        this.comments = [message, ...this.comments];
        this.syncChatMessages();
        this.cdr.markForCheck();
      },
    );
  }

  ngOnDestroy(): void {
    this.chatUnsubscribe?.();
  }

  loadAll(): void {
    this.requestService.getById(this.requestId).subscribe({
      next: (r) => {
        this.request = r;
        this.newStatus = r.status;
        this.assignEmployeeId = r.assignedEmployeeId || null;
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
      this.syncChatMessages();
      this.cdr.markForCheck();
    } });
    this.loadDocuments();
    if (this.isStaff) {
      this.requestService.history(this.requestId).subscribe({ next: (h) => {
        this.history = h;
        this.groupHistory();
        this.cdr.markForCheck();
      } });
      this.loadTasks();
    }
  }

  groupedHistory: { actorName: string; items: any[] }[] = [];
  
  groupHistory(): void {
    this.groupedHistory = [];
    if (!this.history?.length) return;
    let currentGroup = { actorName: this.history[0].actorName, items: [this.history[0]] };
    for (let i = 1; i < this.history.length; i++) {
      const item = this.history[i];
      if (item.actorName === currentGroup.actorName) {
        currentGroup.items.push(item);
      } else {
        this.groupedHistory.push(currentGroup);
        currentGroup = { actorName: item.actorName, items: [item] };
      }
    }
    this.groupedHistory.push(currentGroup);
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
    if (!confirm('Are you sure you want to assign this request to the selected employee?')) return;
    this.error = '';
    this.info = '';
    this.cdr.markForCheck();
    this.requestService.assign(this.requestId, this.assignEmployeeId).subscribe({
      next: (r) => {
        this.request = r;
        this.info = 'Assigned successfully';
        this.editingAssignment = false;
        this.loadAll();
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
    if (!this.newTask.title?.trim() || !this.newTask.assignedEmployeeId || !this.newTask.dueDate) return;
    this.requestService.addTask(this.requestId, this.newTask).subscribe({
      next: () => {
        this.newTask = { title: '', assignedEmployeeId: null, dueDate: '', priority: 'NORMAL', estimatedHours: null, description: '' };
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

  // ----- Documents (client or staff) -----

  loadDocuments(): void {
    this.requestService.documents(this.requestId).subscribe({
      next: (docs) => { this.documents = docs; this.cdr.markForCheck(); },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading = true;
    this.error = '';
    this.cdr.markForCheck();

    this.api.uploadFile(file).subscribe({
      next: (uploaded) => {
        this.requestService.addDocument(this.requestId, {
          fileName: uploaded.fileName,
          fileUrl: uploaded.fileUrl,
          fileType: file.type,
          fileSizeBytes: file.size,
          label: this.documentLabel.trim() || undefined,
        }).subscribe({
          next: () => {
            this.documentLabel = '';
            this.uploading = false;
            this.loadDocuments();
            this.cdr.markForCheck();
          },
          error: (err) => {
            this.error = err?.error?.message || 'Failed to attach document';
            this.uploading = false;
            this.cdr.markForCheck();
          },
        });
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to upload file';
        this.uploading = false;
        this.cdr.markForCheck();
      },
    });
    input.value = '';
  }

  deleteDocument(doc: RequestDocument): void {
    this.requestService.deleteDocument(this.requestId, doc.id).subscribe({
      next: () => { this.loadDocuments(); },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete document';
        this.cdr.markForCheck();
      },
    });
  }

  postChatMessage(text: string): void {
    this.posting = true;
    this.cdr.markForCheck();
    // The live push (pushChatMessage on the backend) only reaches the *other*
    // party, never the sender - refetch so this tab sees its own message too.
    this.requestService.addComment(this.requestId, text).subscribe({
      next: () => {
        this.posting = false;
        this.loadAll();
      },
      error: (err) => {
        this.posting = false;
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

  summarise(): void {
    if (this.summarising) return;
    this.summarising = true;
    this.summaryError = '';
    this.cdr.markForCheck();
    this.requestService.summarise(this.requestId).subscribe({
      next: (r) => {
        if (this.request) this.request.aiSummary = r.aiSummary;
        this.summarising = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.summaryError = err?.error?.message || 'Failed to generate summary';
        this.summarising = false;
        this.cdr.markForCheck();
      },
    });
  }
}
