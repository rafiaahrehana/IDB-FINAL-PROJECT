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
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-services',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog, HasPermissionDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './services.html',
  styleUrls: ['./services.scss']
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

  // COLOR PALETTES FOR DIFFERENT SERVICES CARDS
  private colorPalettes = [
    { border: 'rgba(59, 130, 246, 0.2)', hover: 'rgba(59, 130, 246, 0.8)', bg: 'rgba(59, 130, 246, 0.08)', text: '#3b82f6' }, // Blue
    { border: 'rgba(16, 185, 129, 0.2)', hover: 'rgba(16, 185, 129, 0.8)', bg: 'rgba(16, 185, 129, 0.08)', text: '#10b981' }, // Green
    { border: 'rgba(245, 158, 11, 0.2)', hover: 'rgba(245, 158, 11, 0.8)', bg: 'rgba(245, 158, 11, 0.08)', text: '#f59e0b' }, // Amber
    { border: 'rgba(139, 92, 246, 0.2)', hover: 'rgba(139, 92, 246, 0.8)', bg: 'rgba(139, 92, 246, 0.08)', text: '#8b5cf6' }, // Purple
    { border: 'rgba(244, 63, 94, 0.2)', hover: 'rgba(244, 63, 94, 0.8)', bg: 'rgba(244, 63, 94, 0.08)', text: '#f43f5e' }, // Rose
    { border: 'rgba(6, 182, 212, 0.2)', hover: 'rgba(6, 182, 212, 0.8)', bg: 'rgba(6, 182, 212, 0.08)', text: '#06b6d4' }, // Cyan
    { border: 'rgba(236, 72, 153, 0.2)', hover: 'rgba(236, 72, 153, 0.8)', bg: 'rgba(236, 72, 153, 0.08)', text: '#ec4899' }, // Pink
    { border: 'rgba(99, 102, 241, 0.2)', hover: 'rgba(99, 102, 241, 0.8)', bg: 'rgba(99, 102, 241, 0.08)', text: '#6366f1' }, // Indigo
  ];

  getPalette(s: CompanyService, index: number) {
    const cat = (s.categoryName || '').toLowerCase().trim();
    if (cat.includes('web') || cat.includes('software') || cat.includes('commerce') || cat.includes('app')) {
      return { border: 'rgba(139, 92, 246, 0.25)', hover: 'rgba(139, 92, 246, 0.8)', bg: 'rgba(139, 92, 246, 0.08)', text: '#8b5cf6' }; // Purple
    }
    if (cat.includes('it') || cat.includes('tech') || cat.includes('support')) {
      return { border: 'rgba(59, 130, 246, 0.25)', hover: 'rgba(59, 130, 246, 0.8)', bg: 'rgba(59, 130, 246, 0.08)', text: '#3b82f6' }; // Blue
    }
    if (cat.includes('consult') || cat.includes('architect')) {
      return { border: 'rgba(16, 185, 129, 0.25)', hover: 'rgba(16, 185, 129, 0.8)', bg: 'rgba(16, 185, 129, 0.08)', text: '#10b981' }; // Green
    }
    if (cat.includes('legal') || cat.includes('complian') || cat.includes('incorporat')) {
      return { border: 'rgba(217, 119, 6, 0.25)', hover: 'rgba(217, 119, 6, 0.8)', bg: 'rgba(217, 119, 6, 0.08)', text: '#c2410c' }; // Orange/Brown
    }
    if (cat.includes('office') || cat.includes('space') || cat.includes('room')) {
      return { border: 'rgba(245, 158, 11, 0.25)', hover: 'rgba(245, 158, 11, 0.8)', bg: 'rgba(245, 158, 11, 0.08)', text: '#d97706' }; // Amber/Orange
    }
    if (cat.includes('design') || cat.includes('graphic')) {
      return { border: 'rgba(6, 182, 212, 0.25)', hover: 'rgba(6, 182, 212, 0.8)', bg: 'rgba(6, 182, 212, 0.08)', text: '#06b6d4' }; // Cyan/Teal
    }
    if (cat.includes('market') || cat.includes('digital') || cat.includes('seo')) {
      return { border: 'rgba(236, 72, 153, 0.25)', hover: 'rgba(236, 72, 153, 0.8)', bg: 'rgba(236, 72, 153, 0.08)', text: '#ec4899' }; // Pink
    }
    return this.colorPalettes[index % this.colorPalettes.length];
  }

  getServiceBorderColor(s: CompanyService, index: number): string {
    return this.getPalette(s, index).border;
  }

  getServiceHoverBorderColor(s: CompanyService, index: number): string {
    return this.getPalette(s, index).hover;
  }

  getIconBg(s: CompanyService, index: number): string {
    return this.getPalette(s, index).bg;
  }

  getIconColor(s: CompanyService, index: number): string {
    return this.getPalette(s, index).text;
  }

  getServiceIcon(name: string): string {
    const n = name.toLowerCase();
    if (n.includes('android') || n.includes('app')) return 'bi-code-slash';
    if (n.includes('email') || n.includes('mail') || n.includes('g-suite')) return 'bi-envelope';
    if (n.includes('cloud') || n.includes('architecture') || n.includes('server') || n.includes('hosting')) return 'bi-cloud';
    if (n.includes('incorporation') || n.includes('rjsc') || n.includes('company') || n.includes('legal')) return 'bi-bank';
    if (n.includes('room') || n.includes('space') || n.includes('conference')) return 'bi-door-open';
    if (n.includes('website') || n.includes('web') || n.includes('portal')) return 'bi-globe';
    if (n.includes('e-commerce') || n.includes('shop') || n.includes('cart') || n.includes('sales')) return 'bi-cart';
    if (n.includes('design') || n.includes('graphic') || n.includes('art') || n.includes('logo')) return 'bi-brush';
    if (n.includes('marketing') || n.includes('digital') || n.includes('seo') || n.includes('ads')) return 'bi-megaphone';
    if (n.includes('support') || n.includes('maintenance') || n.includes('help') || n.includes('it ')) return 'bi-headset';
    return 'bi-briefcase';
  }
}
