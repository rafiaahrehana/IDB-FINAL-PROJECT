import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Employee, HrAsset, HrAssetRequest } from '../../models/hrm.model';
import { HrAssetService } from '../../services/hr-asset.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-hr-assets',
  imports: [CommonModule, FormsModule, Pagination, Loader, EmptyState, ConfirmDialog],
  templateUrl: './assets.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Assets implements OnInit {
  // VARIABLES
  assets: HrAsset[] = [];
  employees: Employee[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: HrAssetRequest = { name: '' };

  assignTarget: HrAsset | null = null;
  assignEmployeeId: number | null = null;
  unassignTarget: HrAsset | null = null;
  deleteTarget: HrAsset | null = null;

  constructor(
    private assetService: HrAssetService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD ASSETS
  load(): void {
    this.loading = true;
    this.error = '';
    this.assetService.list(this.page).subscribe({
      next: (res) => { this.assets = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load assets'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // LAZY LOAD EMPLOYEES FOR ASSIGN DROPDOWN
  loadEmployees(): void {
    if (this.employees.length) return;
    this.employeeService.list(0, 200).subscribe({
      next: (res) => { this.employees = res.content; this.cdr.markForCheck(); },
      error: () => { this.employees = []; this.cdr.markForCheck(); }
    });
  }

  // OPEN CREATE / EDIT FORM
  openCreate(): void {
    this.editingId = null;
    this.form = { name: '' };
    this.showForm = true;
    this.loadEmployees();
  }

  openEdit(a: HrAsset): void {
    this.editingId = a.id;
    this.form = {
      name: a.name,
      category: a.category,
      serialNumber: a.serialNumber,
      description: a.description,
      purchaseDate: a.purchaseDate,
      purchaseCost: a.purchaseCost,
      assignedToId: a.assignedToId,
      notes: a.notes,
    };
    this.showForm = true;
    this.loadEmployees();
  }

  // SAVE ASSET
  save(): void {
    const op = this.editingId
      ? this.assetService.update(this.editingId, this.form)
      : this.assetService.create(this.form);
    op.subscribe({
      next: () => {
        this.success = this.editingId ? 'Asset updated' : 'Asset created';
        this.showForm = false; this.editingId = null; this.cdr.markForCheck(); this.load();
      },
      error: (err) => { this.error = err?.error?.message || 'Failed to save asset'; this.cdr.markForCheck(); }
    });
  }

  // OPEN ASSIGN DIALOG
  openAssign(a: HrAsset): void {
    this.assignTarget = a;
    this.assignEmployeeId = null;
    this.loadEmployees();
  }

  // ASSIGN ASSET
  doAssign(): void {
    if (!this.assignTarget || !this.assignEmployeeId) return;
    this.assetService.assign(this.assignTarget.id, this.assignEmployeeId).subscribe({
      next: () => { this.assignTarget = null; this.success = 'Asset assigned'; this.cdr.markForCheck(); this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to assign'; this.assignTarget = null; this.cdr.markForCheck(); }
    });
  }

  // UNASSIGN ASSET
  doUnassign(): void {
    if (!this.unassignTarget) return;
    this.assetService.unassign(this.unassignTarget.id).subscribe({
      next: () => { this.unassignTarget = null; this.success = 'Asset unassigned'; this.cdr.markForCheck(); this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to unassign'; this.unassignTarget = null; this.cdr.markForCheck(); }
    });
  }

  // DELETE ASSET
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.assetService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Asset deleted'; this.cdr.markForCheck(); this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete asset'; this.cdr.markForCheck(); }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // STATUS BADGE CLASS
  statusClass(status: string): string {
    return {
      AVAILABLE: 'text-bg-success',
      ASSIGNED: 'text-bg-primary',
      UNDER_MAINTENANCE: 'text-bg-warning',
      DISPOSED: 'text-bg-secondary',
    }[status] || 'text-bg-secondary';
  }
}
