import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { WebsitePerson } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';
import { FileUpload } from '../../../../../shared/components/file-upload/file-upload';
import { FileUploadResult } from '../../../../../shared/services/file-upload.service';

function emptyForm(): WebsitePerson {
  return { name: '', role: '', company: '', quote: '', avatarUrl: '', rating: 5 };
}

@Component({
  selector: 'app-testimonials-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog, FileUpload],
  templateUrl: './testimonials-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestimonialsTab implements OnInit {
  testimonials: WebsitePerson[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editing: WebsitePerson | null = null;
  form: WebsitePerson = emptyForm();
  deleteTarget: WebsitePerson | null = null;

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listTestimonials().subscribe({
      next: (list) => { this.testimonials = list; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load testimonials'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(t: WebsitePerson): void {
    this.editing = t;
    this.form = { ...t };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  onAvatarUploaded(result: FileUploadResult): void {
    this.form.avatarUrl = result.fileUrl;
  }

  save(): void {
    if (!this.form.name?.trim() || !this.form.quote?.trim()) {
      this.error = 'Name and quote are required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const obs = this.editing?.id
      ? this.service.updateTestimonial(this.editing.id, this.form)
      : this.service.createTestimonial(this.form);
    obs.subscribe({
      next: () => {
        this.success = this.editing ? 'Testimonial updated' : 'Testimonial added';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save testimonial';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deleteTestimonial(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Testimonial removed';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to remove testimonial';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
