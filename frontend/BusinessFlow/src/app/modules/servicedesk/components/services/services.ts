import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CompanyService,
  CompanyServiceRequest,
  ServiceCategory,
  WorkflowTemplate,
  SERVICE_PRICE_TYPES,
  SERVICE_REQUEST_PRIORITIES,
  SERVICE_VISIBILITIES,
} from '../../models/servicedesk.model';
import { CompanyServiceService } from '../../services/company-service.service';
import { ServiceCategoryService } from '../../services/service-category.service';
import { WorkflowService } from '../../services/workflow.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-services',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './services.html',
})
export class Services implements OnInit {
  // VARIABLES
  services: CompanyService[] = [];
  categories: ServiceCategory[] = [];
  workflows: WorkflowTemplate[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: CompanyServiceRequest = { name: '' };

  deleteTarget: CompanyService | null = null;

  priceTypes = SERVICE_PRICE_TYPES;
  priorities = SERVICE_REQUEST_PRIORITIES;
  visibilities = SERVICE_VISIBILITIES;

  constructor(
    private serviceService: CompanyServiceService,
    private categoryService: ServiceCategoryService,
    private workflowService: WorkflowService,
    private cdr: ChangeDetectorRef,
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD SERVICES
  load(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    this.serviceService.list(this.page).subscribe({
      next: (res) => { this.services = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load services'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // LAZY LOAD LOOKUPS FOR THE FORM DROPDOWNS
  loadLookups(): void {
    if (!this.categories.length) {
      this.categoryService.lookup().subscribe({ next: (res) => { this.categories = res; this.cdr.markForCheck(); }, error: () => { this.categories = []; this.cdr.markForCheck(); } });
    }
    if (!this.workflows.length) {
      this.workflowService.listActive().subscribe({ next: (res) => { this.workflows = res; this.cdr.markForCheck(); }, error: () => { this.workflows = []; this.cdr.markForCheck(); } });
    }
  }

  // OPEN CREATE / EDIT FORM
  openCreate(): void {
    this.editingId = null;
    this.form = { name: '', priceType: 'FIXED', visibility: 'PUBLIC' };
    this.showForm = true;
    this.loadLookups();
  }

  openEdit(s: CompanyService): void {
    this.editingId = s.id;
    this.form = {
      name: s.name,
      nameBn: s.nameBn,
      description: s.description,
      descriptionBn: s.descriptionBn,
      price: s.price,
      priceType: s.priceType,
      estimatedDays: s.estimatedDays,
      defaultPriority: s.defaultPriority,
      categoryId: s.categoryId,
      workflowTemplateId: s.workflowTemplateId,
      serviceTemplateId: s.serviceTemplateId,
      currency: s.currency,
      featured: s.featured,
      remote: s.remote,
      onSite: s.onSite,
      online: s.online,
      maximumOrders: s.maximumOrders,
      autoApproval: s.autoApproval,
    };
    this.showForm = true;
    this.loadLookups();
  }

  // SAVE SERVICE
  save(): void {
    const op = this.editingId
      ? this.serviceService.update(this.editingId, this.form)
      : this.serviceService.create(this.form);
    op.subscribe({
      next: () => {
        this.success = this.editingId ? 'Service updated' : 'Service created';
        this.showForm = false; this.editingId = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed to save service'; this.cdr.markForCheck(); }
    });
  }

  // TOGGLE ACTIVE
  toggle(s: CompanyService): void {
    this.serviceService.toggle(s.id).subscribe({
      next: () => this.load(),
      error: (err) => { this.error = err?.error?.message || 'Failed to toggle service'; this.cdr.markForCheck(); }
    });
  }

  // DELETE SERVICE
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.serviceService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Service deleted'; this.cdr.markForCheck(); this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete service'; this.cdr.markForCheck(); }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // LABELS
  priceTypeLabel(type?: string): string {
    return type ? type.charAt(0) + type.slice(1).toLowerCase() : '-';
  }
}
