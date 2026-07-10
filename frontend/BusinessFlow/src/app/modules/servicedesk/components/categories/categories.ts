import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServiceCategory, ServiceCategoryRequest } from '../../models/servicedesk.model';
import { ServiceCategoryService } from '../../services/service-category.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-service-categories',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState],
  templateUrl: './categories.html',
})
export class Categories implements OnInit {
  // VARIABLES
  categories: ServiceCategory[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: ServiceCategoryRequest = { name: '' };

  constructor(private categoryService: ServiceCategoryService) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD CATEGORIES
  load(): void {
    this.loading = true;
    this.error = '';
    this.categoryService.list().subscribe({
      next: (res) => { this.categories = res; this.totalPages = 1; this.loading = false; },
      error: () => { this.error = 'Failed to load categories'; this.loading = false; }
    });
  }

  // OPEN CREATE / EDIT FORM
  openCreate(): void {
    this.editingId = null;
    this.form = { name: '' };
    this.showForm = true;
  }

  openEdit(c: ServiceCategory): void {
    this.loading = true;
    this.error = '';
    this.categoryService.getById(c.id).subscribe({
      next: (category) => {
        this.editingId = category.id;
        this.form = {
          name: category.name,
          nameBn: category.nameBn,
          description: category.description,
          iconUrl: category.iconUrl,
          sortOrder: category.sortOrder,
        };
        this.showForm = true;
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load category'; this.loading = false; }
    });
  }

  // SAVE CATEGORY
  save(): void {
    const op = this.editingId
      ? this.categoryService.update(this.editingId, this.form)
      : this.categoryService.create(this.form);
    op.subscribe({
      next: () => {
        this.success = this.editingId ? 'Category updated' : 'Category created';
        this.showForm = false; this.editingId = null; this.load();
      },
      error: (err) => this.error = err?.error?.message || 'Failed to save category'
    });
  }

  // TOGGLE ACTIVE
  toggle(c: ServiceCategory): void {
    this.categoryService.toggle(c.id).subscribe({
      next: () => this.load(),
      error: (err) => this.error = err?.error?.message || 'Failed to toggle category'
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }
}
