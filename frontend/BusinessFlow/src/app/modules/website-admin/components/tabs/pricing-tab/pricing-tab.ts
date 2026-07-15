import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsiteAdminService } from '../../../services/website-admin.service';
import { PricingPlan } from '../../../models/website-admin.model';
import { Loader } from '../../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../../shared/components/confirm-dialog/confirm-dialog';

function emptyForm(): PricingPlan {
  return { name: '', description: '', price: '', period: '', cta: 'Get Started', featured: false, features: [] };
}

@Component({
  selector: 'app-pricing-tab',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog],
  templateUrl: './pricing-tab.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PricingTab implements OnInit {
  plans: PricingPlan[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editing: PricingPlan | null = null;
  form: PricingPlan = emptyForm();
  newFeature = '';
  deleteTarget: PricingPlan | null = null;

  constructor(private service: WebsiteAdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listPricingPlans().subscribe({
      next: (plans) => { this.plans = plans; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load pricing plans'; this.loading = false; this.cdr.markForCheck(); },
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form = emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(p: PricingPlan): void {
    this.editing = p;
    this.form = { ...p, features: [...(p.features || [])] };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  addFeature(): void {
    const f = this.newFeature.trim();
    if (!f) return;
    this.form.features.push(f);
    this.newFeature = '';
  }

  removeFeature(index: number): void {
    this.form.features.splice(index, 1);
  }

  save(): void {
    if (!this.form.name?.trim()) {
      this.error = 'Plan name is required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const obs = this.editing?.id
      ? this.service.updatePricingPlan(this.editing.id, this.form)
      : this.service.createPricingPlan(this.form);
    obs.subscribe({
      next: () => {
        this.success = this.editing ? 'Plan updated' : 'Plan created';
        this.saving = false;
        this.showForm = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save plan';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget?.id) return;
    this.service.deletePricingPlan(this.deleteTarget.id).subscribe({
      next: () => {
        this.success = 'Plan deleted';
        this.deleteTarget = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete plan';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }
}
