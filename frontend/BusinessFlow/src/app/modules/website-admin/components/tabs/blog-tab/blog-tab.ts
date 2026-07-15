import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { WebsiteContent } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';
import { FileUpload } from '../../../../../shared/components/file-upload/file-upload';
import { FileUploadResult } from '../../../../../shared/services/file-upload.service';

function emptyForm(): WebsiteContent {
  return { slug: '', title: '', body: '', excerpt: '', coverImageUrl: '', author: '', category: '', readMinutes: undefined };
}

@Component({
  selector: 'app-blog-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog, FileUpload],
  templateUrl: './blog-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BlogTab implements OnInit {
  posts: WebsiteContent[] = [];
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
    this.service.listBlogPosts().subscribe({
      next: (posts) => { this.posts = posts; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load blog posts'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(post: WebsiteContent): void {
    this.editing = post;
    this.form = { ...post };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  onCoverUploaded(result: FileUploadResult): void {
    this.form.coverImageUrl = result.fileUrl;
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
    const obs = this.editing?.id
      ? this.service.updateBlogPost(this.editing.id, this.form)
      : this.service.createBlogPost(this.form);
    obs.subscribe({
      next: () => {
        this.success = this.editing ? 'Post updated' : 'Post created';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save post';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deleteBlogPost(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Post deleted';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete post';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
