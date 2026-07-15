import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { Faq } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';

function emptyForm(): Faq {
  return { question: '', answer: '', category: '' };
}

@Component({
  selector: 'app-faqs-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog],
  templateUrl: './faqs-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FaqsTab implements OnInit {
  faqs: Faq[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editing: Faq | null = null;
  form: Faq = emptyForm();
  deleteTarget: Faq | null = null;

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listFaqs().subscribe({
      next: (faqs) => { this.faqs = faqs; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load FAQs'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(faq: Faq): void {
    this.editing = faq;
    this.form = { ...faq };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  save(): void {
    if (!this.form.question?.trim() || !this.form.answer?.trim()) {
      this.error = 'Question and answer are required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const obs = this.editing?.id
      ? this.service.updateFaq(this.editing.id, this.form)
      : this.service.createFaq(this.form);
    obs.subscribe({
      next: () => {
        this.success = this.editing ? 'FAQ updated' : 'FAQ created';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save FAQ';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deleteFaq(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'FAQ deleted';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete FAQ';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
