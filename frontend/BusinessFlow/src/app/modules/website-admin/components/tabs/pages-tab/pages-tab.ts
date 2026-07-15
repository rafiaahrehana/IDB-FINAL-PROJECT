import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { WebsiteContent } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';

function emptyForm(): WebsiteContent {
  return { slug: '', title: '', body: '' };
}

@Component({
  selector: 'app-pages-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog],
  templateUrl: './pages-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PagesTab implements OnInit {
  pages: WebsiteContent[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editing: WebsiteContent | null = null;
  form: WebsiteContent = emptyForm();
  deleteTarget: WebsiteContent | null = null;

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listPages().subscribe({
      next: (pages) => { this.pages = pages; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load pages'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(page: WebsiteContent): void {
    this.editing = page;
    this.form = { ...page };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  save(): void {
    if (!this.form.title?.trim() || !this.form.slug?.trim()) {
      this.error = 'Title and slug are required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const payload = this.editing?.id ? { ...this.form, id: this.editing.id } : this.form;
    this.service.savePage(payload).subscribe({
      next: () => {
        this.success = this.editing ? 'Page updated' : 'Page created';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save page';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deletePage(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Page deleted';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete page';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
