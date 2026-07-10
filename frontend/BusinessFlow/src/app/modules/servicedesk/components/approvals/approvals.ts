import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { StageApproval } from '../../models/servicedesk.model';
import { ApprovalService } from '../../services/approval.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-approvals',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState],
  templateUrl: './approvals.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './approvals.scss',
})
export class Approvals implements OnInit {
  approvals: StageApproval[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  notes: Record<number, string> = {};

  constructor(private approvalService: ApprovalService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.approvalService.pending(this.page).subscribe({
      next: (res) => {
        this.approvals = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load approvals';
        this.loading = false;
      },
    });
  }

  approve(approval: StageApproval): void {
    this.approvalService.approve(approval.id, this.notes[approval.id]).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message || 'Failed to approve'),
    });
  }

  reject(approval: StageApproval): void {
    const notes = (this.notes[approval.id] || '').trim();
    if (!notes) {
      this.error = 'Rejection requires a note explaining the decision';
      return;
    }
    this.approvalService.reject(approval.id, notes).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message || 'Failed to reject'),
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
