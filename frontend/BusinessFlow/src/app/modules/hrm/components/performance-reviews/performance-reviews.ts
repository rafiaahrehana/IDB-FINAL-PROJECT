import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PerformanceReview, PerformanceReviewRequest, Employee } from '../../models/hrm.model';
import { PerformanceReviewService } from '../../services/performance-review.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-performance-reviews',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './performance-reviews.html',
})
export class PerformanceReviews implements OnInit {
  reviews: PerformanceReview[] = [];
  employees: Employee[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  isEdit = false;
  selectedId: number | null = null;
  form: PerformanceReviewRequest = this.emptyForm();

  deleteTarget: PerformanceReview | null = null;

  constructor(
    private reviewService: PerformanceReviewService,
    private employeeService: EmployeeService
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadEmployees();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.reviewService.list(this.page, 20).subscribe({
      next: (res) => {
        this.reviews = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load performance reviews';
        this.loading = false;
      }
    });
  }

  loadEmployees(): void {
    this.employeeService.list(0, 100).subscribe({
      next: (res) => this.employees = res.content,
      error: () => this.employees = []
    });
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.isEdit = false;
    this.showForm = true;
  }

  openEdit(r: PerformanceReview): void {
    this.form = {
      employeeId: r.employeeId,
      reviewPeriodStart: r.reviewPeriodStart,
      reviewPeriodEnd: r.reviewPeriodEnd,
      scoreWorkQuality: r.scoreWorkQuality,
      scoreProductivity: r.scoreProductivity,
      scoreCommunication: r.scoreCommunication,
      scoreTeamwork: r.scoreTeamwork,
      scoreInitiative: r.scoreInitiative,
      scorePunctuality: r.scorePunctuality,
      strengths: r.strengths,
      areasForImprovement: r.areasForImprovement,
      goalsForNextPeriod: r.goalsForNextPeriod,
      comments: r.comments,
    };
    this.selectedId = r.id;
    this.isEdit = true;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const payload = this.cleanPayload();
    const request = this.isEdit && this.selectedId
      ? this.reviewService.update(this.selectedId, payload)
      : this.reviewService.create(payload);

    request.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.isEdit ? 'Performance review updated' : 'Performance review created';
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save review';
      }
    });
  }

  finalise(r: PerformanceReview): void {
    this.reviewService.finalise(r.id).subscribe({
      next: () => {
        this.success = 'Performance review finalised successfully';
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to finalise review';
      }
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.reviewService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Performance review deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Failed to delete review';
      }
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }

  private emptyForm(): PerformanceReviewRequest {
    return {
      employeeId: undefined,
      reviewPeriodStart: '',
      reviewPeriodEnd: '',
      scoreWorkQuality: 5,
      scoreProductivity: 5,
      scoreCommunication: 5,
      scoreTeamwork: 5,
      scoreInitiative: 5,
      scorePunctuality: 5,
      strengths: '',
      areasForImprovement: '',
      goalsForNextPeriod: '',
      comments: '',
    };
  }

  private cleanPayload(): PerformanceReviewRequest {
    const payload: any = { ...this.form };
    return payload;
  }
}
