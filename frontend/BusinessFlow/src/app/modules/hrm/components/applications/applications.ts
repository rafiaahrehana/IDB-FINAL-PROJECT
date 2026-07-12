import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ApplicationStatus,
  APPLICATION_STATUSES,
  JobApplication,
  JobApplicationRequest,
  JobPosting,
} from '../../models/hrm.model';
import { RecruitmentService } from '../../services/recruitment.service';
import { JobPostingService } from '../../services/job-posting.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-applications',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './applications.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Applications implements OnInit {
  // VARIABLES
  applications: JobApplication[] = [];
  jobs: JobPosting[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  statusFilter: ApplicationStatus | '' = '';

  showForm = false;
  selectedJobId: number | null = null;
  form: JobApplicationRequest = { applicantName: '', applicantEmail: '' };

  statusTarget: JobApplication | null = null;
  newStatus: ApplicationStatus = 'APPLIED';
  statusNotes = '';
  deleteTarget: JobApplication | null = null;

  statuses = APPLICATION_STATUSES;

  constructor(
    private recruitmentService: RecruitmentService,
    private jobPostingService: JobPostingService,
    private cdr: ChangeDetectorRef,
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD APPLICATIONS
  load(): void {
    this.loading = true;
    this.error = '';
    this.recruitmentService.list(this.page, 20, this.statusFilter || undefined).subscribe({
      next: (res) => { this.applications = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load applications'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // LOAD OPEN JOB POSTINGS FOR THE APPLY DROPDOWN
  loadJobs(): void {
    if (this.jobs.length) return;
    this.jobPostingService.list(0, 100, 'OPEN').subscribe({
      next: (res) => { this.jobs = res.content; this.cdr.markForCheck(); },
      error: () => { this.jobs = []; this.cdr.markForCheck(); }
    });
  }

  // OPEN APPLY FORM
  openApply(): void {
    this.form = { applicantName: '', applicantEmail: '' };
    this.selectedJobId = null;
    this.showForm = true;
    this.loadJobs();
  }

  // SUBMIT APPLICATION
  apply(): void {
    if (!this.selectedJobId) return;
    this.recruitmentService.apply(this.selectedJobId, this.form).subscribe({
      next: () => { this.showForm = false; this.success = 'Application submitted'; this.cdr.markForCheck(); this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to submit application'; this.cdr.markForCheck(); }
    });
  }

  // OPEN STATUS CHANGE DIALOG
  openStatusChange(a: JobApplication): void {
    this.statusTarget = a;
    this.newStatus = a.status;
    this.statusNotes = a.notes || '';
  }

  // SUBMIT STATUS CHANGE
  doStatusChange(): void {
    if (!this.statusTarget) return;
    this.recruitmentService.updateStatus(this.statusTarget.id, this.newStatus, this.statusNotes).subscribe({
      next: () => { this.statusTarget = null; this.success = 'Application updated'; this.cdr.markForCheck(); this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to update'; this.statusTarget = null; this.cdr.markForCheck(); }
    });
  }

  // DELETE APPLICATION
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.recruitmentService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Application deleted'; this.cdr.markForCheck(); this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete application'; this.cdr.markForCheck(); }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // STATUS BADGE CLASS
  statusClass(status: ApplicationStatus): string {
    return {
      APPLIED: 'text-bg-secondary',
      SCREENING: 'text-bg-info',
      SHORTLISTED: 'text-bg-info',
      INTERVIEW_SCHEDULED: 'text-bg-primary',
      INTERVIEWED: 'text-bg-primary',
      OFFERED: 'text-bg-warning',
      HIRED: 'text-bg-success',
      REJECTED: 'text-bg-danger',
      WITHDRAWN: 'text-bg-secondary',
    }[status] || 'text-bg-secondary';
  }
}
