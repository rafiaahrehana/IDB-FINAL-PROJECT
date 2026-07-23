import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Designation, DesignationRequest } from '../../models/hrm.model';
import { DesignationService } from '../../services/designation.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-designations',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog, HasPermissionDirective],
  templateUrl: './designations.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './designations.scss',
})
export class Designations implements OnInit {
  designations: Designation[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: DesignationRequest = { name: '', code: '', level: 1 };

  deleteTarget: Designation | null = null;

  constructor(private designationService: DesignationService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.designationService.list(this.page, 20).subscribe({
      next: (res) => {
        this.designations = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load designations';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = { name: '', code: '', level: 1 };
    this.showForm = true;
  }

  openEdit(d: Designation): void {
    this.editingId = d.id;
    this.form = { name: d.name, code: d.code, level: d.level, description: d.description };
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const req = this.editingId
      ? this.designationService.update(this.editingId, this.form)
      : this.designationService.create(this.form);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Designation updated' : 'Designation created';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save designation';
        this.cdr.markForCheck();
      },
    });
  }

  toggle(d: Designation): void {
    this.designationService.toggle(d.id).subscribe({
      next: () => this.load(),
      error: () => { this.error = 'Failed to update designation status'; this.cdr.markForCheck(); },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.designationService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Designation deleted';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete designation';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
