import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OffboardingChecklist } from '../../models/itam.model';
import { OffboardingService } from '../../services/offboarding.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-offboarding',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './offboarding.html',
})
export class Offboarding implements OnInit {
  checklists: OffboardingChecklist[] = [];
  pending: OffboardingChecklist[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  newEmployeeId?: number;
  newNotes = '';

  selected: OffboardingChecklist | null = null;
  deleteTarget: OffboardingChecklist | null = null;

  constructor(private offboardingService: OffboardingService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
    this.loadPending();
  }

  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.offboardingService.list(this.page).subscribe({
      next: (res) => {
        this.checklists = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load offboarding checklists';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadPending(): void {
    this.offboardingService.getPending().subscribe({ next: (res) => (this.pending = res) });
  }

  startOffboarding(): void {
    if (!this.newEmployeeId) return;
    this.offboardingService.create({ employeeId: this.newEmployeeId, notes: this.newNotes }).subscribe({
      next: () => {
        this.showForm = false;
        this.newEmployeeId = undefined;
        this.newNotes = '';
        this.success = 'Offboarding checklist created';
        this.load();
        this.loadPending();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to start offboarding'),
    });
  }

  view(c: OffboardingChecklist): void {
    this.selected = c;
  }

  refreshSelected(): void {
    if (!this.selected) return;
    this.offboardingService.getById(this.selected.id).subscribe({ next: (c) => (this.selected = c) });
  }

  markHardware(): void {
    if (!this.selected) return;
    this.offboardingService.markHardwareCollected(this.selected.id).subscribe({
      next: () => {
        this.refreshSelected();
        this.load();
        this.loadPending();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  markLicenses(): void {
    if (!this.selected) return;
    this.offboardingService.markLicensesRevoked(this.selected.id).subscribe({
      next: () => {
        this.refreshSelected();
        this.load();
        this.loadPending();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  markAccess(): void {
    if (!this.selected) return;
    this.offboardingService.markAccessRevoked(this.selected.id).subscribe({
      next: () => {
        this.refreshSelected();
        this.load();
        this.loadPending();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  markData(): void {
    if (!this.selected) return;
    this.offboardingService.markDataHandedOver(this.selected.id).subscribe({
      next: () => {
        this.refreshSelected();
        this.load();
        this.loadPending();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  markExitInterview(): void {
    if (!this.selected) return;
    this.offboardingService.markExitInterviewCompleted(this.selected.id).subscribe({
      next: () => {
        this.refreshSelected();
        this.load();
        this.loadPending();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.offboardingService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        if (this.selected?.id === this.deleteTarget?.id) this.selected = null;
        this.deleteTarget = null;
        this.success = 'Checklist deleted';
        this.load();
        this.loadPending();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete';
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
