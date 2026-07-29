import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  WorkflowStageRequest,
  WorkflowTemplate,
  WorkflowTemplateRequest,
} from '../../models/servicedesk.model';
import { WorkflowService } from '../../services/workflow.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-workflows',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog, HasPermissionDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './workflows.html',
})
export class Workflows implements OnInit {
  // VARIABLES
  templates: WorkflowTemplate[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: WorkflowTemplateRequest = { name: '' };

  // STAGE MANAGEMENT
  stageTarget: WorkflowTemplate | null = null;
  stageForm: WorkflowStageRequest = { name: '' };
  editingStageId: number | null = null;

  deleteTarget: WorkflowTemplate | null = null;
  deleteStageId: number | null = null;

  suggestGoal = '';
  suggesting = false;
  suggestion = '';
  suggestError = '';

  constructor(private workflowService: WorkflowService, private cdr: ChangeDetectorRef) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD TEMPLATES
  load(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    this.workflowService.list(this.page).subscribe({
      next: (res) => { this.templates = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load workflows'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // OPEN CREATE / EDIT TEMPLATE
  openCreate(): void {
    this.editingId = null;
    this.form = { name: '' };
    this.showForm = true;
  }

  openEdit(t: WorkflowTemplate): void {
    this.editingId = t.id;
    this.form = { name: t.name, description: t.description };
    this.showForm = true;
  }

  // SAVE TEMPLATE
  save(): void {
    const op = this.editingId
      ? this.workflowService.update(this.editingId, this.form)
      : this.workflowService.create(this.form);
    op.subscribe({
      next: () => {
        this.success = this.editingId ? 'Workflow updated' : 'Workflow created';
        this.showForm = false; this.editingId = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed to save workflow'; this.cdr.markForCheck(); }
    });
  }

  // TOGGLE ACTIVE
  toggle(t: WorkflowTemplate): void {
    this.workflowService.toggle(t.id).subscribe({
      next: () => this.load(),
      error: (err) => { this.error = err?.error?.message || 'Failed to toggle workflow'; this.cdr.markForCheck(); }
    });
  }

  // DELETE TEMPLATE
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.workflowService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Workflow deleted'; this.cdr.markForCheck(); this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete workflow'; this.cdr.markForCheck(); }
    });
  }

  // STAGE PANEL
  openStages(t: WorkflowTemplate): void {
    // fetch a fresh copy for latest stages
    this.workflowService.getById(t.id).subscribe({
      next: (res) => { this.stageTarget = res; this.resetStageForm(); this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load stages'; this.cdr.markForCheck(); }
    });
  }

  closeStages(): void {
    this.stageTarget = null;
    this.resetStageForm();
  }

  private resetStageForm(): void {
    this.editingStageId = null;
    this.stageForm = { name: '', stageOrder: (this.stageTarget?.stages?.length || 0) + 1 };
  }

  openEditStage(stage: WorkflowStageRequest & { id?: number }): void {
    this.editingStageId = stage.id ?? null;
    this.stageForm = { ...stage };
  }

  saveStage(): void {
    if (!this.stageTarget) return;
    const op = this.editingStageId
      ? this.workflowService.updateStage(this.stageTarget.id, this.editingStageId, this.stageForm)
      : this.workflowService.addStage(this.stageTarget.id, this.stageForm);
    op.subscribe({
      next: () => {
        this.success = this.editingStageId ? 'Stage updated' : 'Stage added';
        this.cdr.markForCheck();
        this.refreshStages();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed to save stage'; this.cdr.markForCheck(); }
    });
  }

  removeStage(stageId: number): void {
    if (!this.stageTarget) return;
    this.workflowService.removeStage(this.stageTarget.id, stageId).subscribe({
      next: () => { this.deleteStageId = null; this.success = 'Stage removed'; this.cdr.markForCheck(); this.refreshStages(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to remove stage'; this.cdr.markForCheck(); }
    });
  }

  private refreshStages(): void {
    if (!this.stageTarget) return;
    this.workflowService.getById(this.stageTarget.id).subscribe({
      next: (res) => { this.stageTarget = res; this.resetStageForm(); this.cdr.markForCheck(); this.load(); }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // AI SUGGESTION
  suggestWorkflow(): void {
    if (!this.suggestGoal.trim() || this.suggesting) return;
    this.suggesting = true;
    this.suggestError = '';
    this.cdr.markForCheck();
    this.workflowService.suggest(this.suggestGoal.trim()).subscribe({
      next: (res) => {
        this.suggestion = res.suggestion;
        this.suggesting = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.suggestError = err?.error?.message || 'Failed to generate suggestion';
        this.suggesting = false;
        this.cdr.markForCheck();
      },
    });
  }
}
