import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { NavItem } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';

function emptyForm(): NavItem {
  return { label: '', url: '', external: false, parentId: null, sortOrder: 0, mega: false };
}

@Component({
  selector: 'app-nav-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog],
  templateUrl: './nav-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavTab implements OnInit {
  items: NavItem[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editing: NavItem | null = null;
  form: NavItem = emptyForm();
  deleteTarget: NavItem | null = null;

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listNavItems().subscribe({
      next: (items) => { this.items = items; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load navigation'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  parentName(parentId: number | null | undefined): string {
    if (!parentId) return '-';
    return this.items.find((i) => i.id === parentId)?.label || '-';
  }

  // A nav item can't be nested under itself or under one of its own descendants
  possibleParents(): NavItem[] {
    return this.items.filter((i) => i.id !== this.editing?.id);
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(item: NavItem): void {
    this.editing = item;
    this.form = { ...item };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  save(): void {
    if (!this.form.label?.trim() || !this.form.url?.trim()) {
      this.error = 'Label and URL are required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const obs = this.editing?.id
      ? this.service.updateNavItem(this.editing.id, this.form)
      : this.service.createNavItem(this.form);
    obs.subscribe({
      next: () => {
        this.success = this.editing ? 'Nav item updated' : 'Nav item created';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save nav item';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deleteNavItem(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Nav item deleted';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete nav item';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
