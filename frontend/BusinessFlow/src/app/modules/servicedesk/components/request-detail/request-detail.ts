import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RequestComment, ServiceRequest, StageApproval } from '../../models/servicedesk.model';
import { ServiceRequestService } from '../../services/service-request.service';
import { ApprovalService } from '../../services/approval.service';

@Component({
  selector: 'app-request-detail',
  imports: [CommonModule, FormsModule],
  templateUrl: './request-detail.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './request-detail.scss',
})
export class RequestDetail implements OnInit {
  requestId!: number;
  request?: ServiceRequest;
  approvals: StageApproval[] = [];
  comments: RequestComment[] = [];
  history: any[] = [];
  newComment = '';
  error = '';
  info = '';

  // Quotation (embedded on the service request - there is no standalone Quotation endpoint)
  quotationForm = { amount: 0, currency: 'USD', notes: '', validUntil: '' };
  rejectReason = '';

  constructor(
    private route: ActivatedRoute,
    private requestService: ServiceRequestService,
    private approvalService: ApprovalService,
  ) {}

  ngOnInit(): void {
    this.requestId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAll();
  }

  loadAll(): void {
    this.requestService.getById(this.requestId).subscribe({
      next: (r) => (this.request = r),
      error: () => (this.error = 'Failed to load request'),
    });
    this.approvalService
      .forRequest(this.requestId)
      .subscribe({ next: (a) => (this.approvals = a) });
    this.requestService.comments(this.requestId).subscribe({ next: (c) => (this.comments = c.content) });
    this.requestService.history(this.requestId).subscribe({ next: (h) => (this.history = h) });
  }

  addComment(): void {
    if (!this.newComment.trim()) return;
    this.requestService.addComment(this.requestId, this.newComment.trim()).subscribe({
      next: () => {
        this.newComment = '';
        this.loadAll();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to add comment'),
    });
  }

  submitQuotation(): void {
    this.error = '';
    this.info = '';
    this.requestService.submitQuotation(this.requestId, this.quotationForm).subscribe({
      next: (r) => {
        this.request = r;
        this.info = 'Quotation submitted';
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to submit quotation'),
    });
  }

  acceptQuotation(): void {
    this.error = '';
    this.info = '';
    this.requestService.acceptQuotation(this.requestId).subscribe({
      next: (r) => {
        this.request = r;
        this.info = 'Quotation accepted';
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to accept quotation'),
    });
  }

  rejectQuotation(): void {
    this.error = '';
    this.info = '';
    this.requestService.rejectQuotation(this.requestId, this.rejectReason || undefined).subscribe({
      next: (r) => {
        this.request = r;
        this.rejectReason = '';
        this.info = 'Quotation rejected';
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to reject quotation'),
    });
  }

  hasPendingApproval(): boolean {
    return this.approvals.some((a) => a.status === 'PENDING');
  }
}
