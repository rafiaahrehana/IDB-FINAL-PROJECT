import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { KbArticle } from '../../models/servicedesk.model';
import { KbService } from '../../services/kb.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-knowledge-base',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  templateUrl: './knowledge-base.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './knowledge-base.scss',
})
export class KnowledgeBase implements OnInit {
  articles: KbArticle[] = [];
  selected?: KbArticle;
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  keyword = '';
  statusFilter = '';

  showEditor = false;
  draft: Partial<KbArticle> = {};

  constructor(private kbService: KbService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    const params: any = {};
    if (this.keyword.trim()) params.keyword = this.keyword.trim();
    if (this.statusFilter) params.status = this.statusFilter;
    this.kbService.list(this.page, 20, params).subscribe({
      next: (res) => {
        this.articles = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load articles';
        this.loading = false;
      },
    });
  }

  open(article: KbArticle): void {
    this.kbService.getById(article.id).subscribe({
      next: (a) => (this.selected = a),
      error: () => (this.error = 'Failed to load article'),
    });
  }

  markHelpful(): void {
    if (!this.selected) return;
    this.kbService.markHelpful(this.selected.id).subscribe({
      next: (a) => (this.selected = a),
    });
  }

  publish(article: KbArticle): void {
    this.kbService.publish(article.id).subscribe({ next: () => this.load() });
  }

  archive(article: KbArticle): void {
    this.kbService.archive(article.id).subscribe({ next: () => this.load() });
  }

  startNew(): void {
    this.draft = { clientVisible: false };
    this.showEditor = true;
    this.selected = undefined;
  }

  saveDraft(): void {
    if (!this.draft.title?.trim() || !this.draft.content?.trim()) {
      this.error = 'Title and content are required';
      return;
    }
    this.kbService.create(this.draft).subscribe({
      next: () => {
        this.showEditor = false;
        this.draft = {};
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed to save article'),
    });
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
