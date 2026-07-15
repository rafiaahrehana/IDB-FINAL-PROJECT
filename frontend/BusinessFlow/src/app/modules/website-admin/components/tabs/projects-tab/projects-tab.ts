import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { PortalProject } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';
import { FileUpload } from '../../../../../shared/components/file-upload/file-upload';
import { FileUploadResult } from '../../../../../shared/services/file-upload.service';

function emptyForm(): PortalProject {
  return { title: '', summary: '', description: '', coverImageUrl: '', client: '', category: '', year: new Date().getFullYear(), tags: [] };
}

@Component({
  selector: 'app-projects-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog, FileUpload],
  templateUrl: './projects-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectsTab implements OnInit {
  projects: PortalProject[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editing: PortalProject | null = null;
  form: PortalProject = emptyForm();
  newTag = '';
  deleteTarget: PortalProject | null = null;

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listProjects().subscribe({
      next: (projects) => { this.projects = projects; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load projects'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(p: PortalProject): void {
    this.editing = p;
    this.form = { ...p, tags: [...(p.tags || [])] };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  onCoverUploaded(result: FileUploadResult): void {
    this.form.coverImageUrl = result.fileUrl;
  }

  addTag(): void {
    const tag = this.newTag.trim();
    if (!tag || this.form.tags.includes(tag)) return;
    this.form.tags.push(tag);
    this.newTag = '';
  }

  removeTag(index: number): void {
    this.form.tags.splice(index, 1);
  }

  save(): void {
    if (!this.form.title?.trim()) {
      this.error = 'Title is required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const obs = this.editing?.id
      ? this.service.updateProject(this.editing.id, this.form)
      : this.service.createProject(this.form);
    obs.subscribe({
      next: () => {
        this.success = this.editing ? 'Project updated' : 'Project added';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save project';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deleteProject(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Project removed';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to remove project';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
