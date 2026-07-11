import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServiceCategory, ServiceCategoryRequest } from '../../models/servicedesk.model';
import { ServiceCategoryService } from '../../services/service-category.service';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

@Component({
  selector: 'app-service-categories',
  imports: [CommonModule, FormsModule, Loader, EmptyState],
  templateUrl: './categories.html',
})
export class Categories implements OnInit {
  // VARIABLES
  categories: ServiceCategory[] = [];
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: ServiceCategoryRequest = { name: '' };

  constructor(private categoryService: ServiceCategoryService, private cdr: ChangeDetectorRef) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD CATEGORIES (management listing - includes inactive, bare list, not paged)
  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.categoryService.listAll().subscribe({
      next: (res) => { this.categories = res; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load categories'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // OPEN CREATE / EDIT FORM
  openCreate(): void {
    this.editingId = null;
    this.form = { name: '' };
    this.showForm = true;
  }

  openEdit(c: ServiceCategory): void {
    this.editingId = c.id;
    this.form = {
      name: c.name,
      nameBn: c.nameBn,
      description: c.description,
      iconUrl: c.iconUrl,
      sortOrder: c.sortOrder,
    };
    this.showForm = true;
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
}
