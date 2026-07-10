import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HrAsset, HrAssetRequest } from '../../../hrm/models/hrm.model';
import { HrAssetService } from '../../../hrm/services/hr-asset.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

// NOTE: Hardware now reuses the shared hrm/asset backend (AssetController, "/api/hr/assets")
// rather than a dedicated ITAM hardware controller (none exists). The Asset entity has
// IT-hardware-specific columns (ipAddress, macAddress, processorModel, ramSize,
// storageSize, operatingSystem, assetTag, brand, model, warrantyExpiry) but
// AssetResponse/AssetRequest currently do NOT expose them - only the generic fields
// below are reachable through the API today. "Category" is used as a free-text way to
// mark hardware type (Laptop, Monitor, etc.) since there is no dedicated hardware-type
// enum on the backend.
@Component({
  selector: 'app-hardware',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './hardware.html',
})
export class Hardware implements OnInit {
  assets: HrAsset[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';
  showForm = false;
  editId: number | null = null;
  form: HrAssetRequest = { name: '' };
  deleteTarget: HrAsset | null = null;
  assignTarget: HrAsset | null = null;
  assignEmployeeId?: number;

  categories = ['Laptop', 'Desktop', 'Monitor', 'Printer', 'Phone', 'Tablet', 'Server', 'Networking', 'Other'];
  today = new Date().toISOString().slice(0, 10);

  constructor(private assetService: HrAssetService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.assetService.list(this.page).subscribe({
      next: (res) => {
        this.assets = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load hardware assets';
        this.loading = false;
      },
    });
  }

  openCreate(): void {
    this.editId = null;
    this.form = { name: '' };
    this.showForm = true;
  }

  openEdit(a: HrAsset): void {
    this.editId = a.id;
    this.form = {
      name: a.name,
      category: a.category,
      serialNumber: a.serialNumber,
      description: a.description,
      purchaseDate: a.purchaseDate,
      purchaseCost: a.purchaseCost,
      notes: a.notes,
      assetTag: a.assetTag,
      brand: a.brand,
      model: a.model,
      ipAddress: a.ipAddress,
      macAddress: a.macAddress,
      processorModel: a.processorModel,
      ramSize: a.ramSize,
      storageSize: a.storageSize,
      operatingSystem: a.operatingSystem,
      warrantyExpiry: a.warrantyExpiry,
    };
    this.showForm = true;
  }

  save(): void {
    const op = this.editId
      ? this.assetService.update(this.editId, this.form)
      : this.assetService.create(this.form);
    op.subscribe({
      next: () => {
        this.showForm = false;
        this.success = 'Saved';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  doAssign(): void {
    if (!this.assignTarget || !this.assignEmployeeId) return;
    this.assetService.assign(this.assignTarget.id, this.assignEmployeeId).subscribe({
      next: () => {
        this.assignTarget = null;
        this.success = 'Assigned';
        this.load();
      },
      error: (err) => {
        this.assignTarget = null;
        this.error = err?.error?.message || 'Failed';
      },
    });
  }

  unassign(a: HrAsset): void {
    this.assetService.unassign(a.id).subscribe({
      next: () => {
        this.success = 'Returned';
        this.load();
      },
      error: (err) => (this.error = err?.error?.message || 'Failed'),
    });
  }

  doDelete(): void {
    if (!this.deleteTarget) return;
    this.assetService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Deleted';
        this.load();
      },
      error: () => {
        this.deleteTarget = null;
        this.error = 'Cannot delete';
      },
    });
  }

  statusClass(s: string): string {
    return (
      { AVAILABLE: 'text-bg-success', ASSIGNED: 'text-bg-primary', UNDER_MAINTENANCE: 'text-bg-warning', DISPOSED: 'text-bg-secondary' }[s] ||
      'text-bg-light'
    );
  }

  goToPage(p: number): void {
    this.page = p;
    this.load();
  }
}
