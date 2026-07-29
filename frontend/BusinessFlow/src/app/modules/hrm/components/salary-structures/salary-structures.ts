import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SalaryStructure, SalaryStructureRequest, Employee } from '../../models/hrm.model';
import { SalaryStructureService } from '../../services/salary-structure.service';
import { EmployeeService } from '../../services/employee.service';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-salary-structures',
  imports: [CommonModule, FormsModule, Loader, EmptyState, ConfirmDialog, HasPermissionDirective],
  templateUrl: './salary-structures.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './salary-structures.scss',
})
export class SalaryStructures implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  structures: SalaryStructure[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editingId: number | null = null;
  form: SalaryStructureRequest = this.emptyForm();

  deleteTarget: SalaryStructure | null = null;

  // ── Auto-breakup presets (Bangladesh-typical component ratios of Gross) ──
  selectedPreset = '';
  breakupPresets = [
    { key: 'BASIC_50', label: 'Standard — Basic 50%', basic: 0.50, houseRent: 0.25, medical: 0.10, transport: 0.05, food: 0.05 },
    { key: 'BASIC_60', label: 'Basic 60%',            basic: 0.60, houseRent: 0.20, medical: 0.10, transport: 0.05, food: 0.05 },
  ];
  pfRate = 0.10; // Provident Fund = 10% of Basic (editable after auto-fill)
  taxRatePct: number | null = null; // Tax as a % of Gross → auto-fills Tax Deduction

  private round(n: number): number {
    return Math.round(n * 100) / 100;
  }

  // Placeholder hint showing each component's % of Gross for the active preset
  // (falls back to the Standard preset so the guidance is always meaningful).
  pctHint(field: 'basic' | 'houseRent' | 'medical' | 'transport' | 'food'): string {
    const p = this.breakupPresets.find((x) => x.key === this.selectedPreset) || this.breakupPresets[0];
    return Math.round(p[field] * 100) + '% of gross';
  }

  // Bottom-up: as components are edited, Gross becomes their running total so the
  // structure always reconciles. Tax (a % of Gross) is refreshed to match.
  syncGross(): void {
    this.form.grossSalary = this.round(this.totalEarnings) as any;
    if (this.taxRatePct) {
      this.form.taxDeduction = this.round(Number(this.form.grossSalary) * Number(this.taxRatePct) / 100) as any;
    }
    this.cdr.markForCheck();
  }

  // Tax deduction from a % of Gross (editable amount afterward).
  applyTax(): void {
    const gross = Number(this.form.grossSalary) || 0;
    const rate = Number(this.taxRatePct) || 0;
    this.form.taxDeduction = this.round(gross * rate / 100) as any;
    this.cdr.markForCheck();
  }

  // Fill the component fields from Gross using the chosen preset. Special Allowance
  // takes the remainder so earnings always reconcile to Gross. Everything stays editable.
  applyBreakup(): void {
    const gross = Number(this.form.grossSalary) || 0;
    const p = this.breakupPresets.find((x) => x.key === this.selectedPreset);
    if (!gross || !p) return;

    this.form.basicSalary = this.round(gross * p.basic) as any;
    this.form.houseRent = this.round(gross * p.houseRent) as any;
    this.form.medicalAllowance = this.round(gross * p.medical) as any;
    this.form.transportAllowance = this.round(gross * p.transport) as any;
    this.form.foodAllowance = this.round(gross * p.food) as any;

    const allocated =
      Number(this.form.basicSalary) + Number(this.form.houseRent) +
      Number(this.form.medicalAllowance) + Number(this.form.transportAllowance) +
      Number(this.form.foodAllowance);
    this.form.specialAllowance = this.round(Math.max(0, gross - allocated)) as any;

    this.form.providentFund = this.round(Number(this.form.basicSalary) * this.pfRate) as any;
    if (this.taxRatePct) this.form.taxDeduction = this.round(gross * Number(this.taxRatePct) / 100) as any;
    this.cdr.markForCheck();
  }

  get totalEarnings(): number {
    const f = this.form;
    return (Number(f.basicSalary) || 0) + (Number(f.houseRent) || 0) +
      (Number(f.medicalAllowance) || 0) + (Number(f.transportAllowance) || 0) +
      (Number(f.foodAllowance) || 0) + (Number(f.specialAllowance) || 0);
  }

  get netSalary(): number {
    return (Number(this.form.grossSalary) || 0) - (Number(this.form.providentFund) || 0) - (Number(this.form.taxDeduction) || 0);
  }

  // True when component earnings add up to Gross (within a rounding cent).
  get earningsReconcile(): boolean {
    const gross = Number(this.form.grossSalary) || 0;
    return gross > 0 && Math.abs(this.totalEarnings - gross) < 0.01;
  }

  constructor(
    private salaryService: SalaryStructureService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeService.list(0, 100).subscribe({ next: (res) => { this.employees = res.content; this.cdr.markForCheck(); } });
  }

  load(): void {
    if (!this.selectedEmployeeId) {
      this.structures = [];
      return;
    }
    this.loading = true;
    this.error = '';
    this.salaryService.history(this.selectedEmployeeId).subscribe({
      next: (res) => {
        this.structures = res;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load salary structures';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreate(): void {
    if (!this.selectedEmployeeId) return;
    this.editingId = null;
    this.form = this.emptyForm();
    this.form.employeeId = this.selectedEmployeeId;
    this.selectedPreset = '';
    this.taxRatePct = null;
    this.showForm = true;
  }

  // Only the current (non-superseded) structure is editable.
  openEdit(s: SalaryStructure): void {
    this.editingId = s.id;
    this.selectedPreset = '';
    this.taxRatePct = null;
    this.form = {
      employeeId: s.employeeId,
      effectiveFrom: s.effectiveFrom,
      grossSalary: s.grossSalary,
      basicSalary: s.basicSalary,
      houseRent: s.houseRent,
      medicalAllowance: s.medicalAllowance,
      transportAllowance: s.transportAllowance,
      foodAllowance: s.foodAllowance,
      specialAllowance: s.specialAllowance,
      providentFund: s.providentFund,
      taxDeduction: s.taxDeduction,
      notes: s.notes,
    } as SalaryStructureRequest;
    this.error = '';
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null || payload[k] === undefined) delete payload[k];
    });
    const op = this.editingId
      ? this.salaryService.update(this.editingId, payload)
      : this.salaryService.create(payload);
    op.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.success = this.editingId ? 'Salary structure updated' : 'Salary structure created';
        this.editingId = null;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to save salary structure';
        this.cdr.markForCheck();
      },
    });
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.salaryService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.deleteTarget = null;
        this.success = 'Salary structure deleted';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete';
        this.deleteTarget = null;
        this.cdr.markForCheck();
      },
    });
  }

  isCurrent(s: SalaryStructure): boolean {
    return !s.effectiveTo;
  }

  private emptyForm(): SalaryStructureRequest {
    return {
      employeeId: undefined as any,
      effectiveFrom: '',
      grossSalary: undefined as any,
      basicSalary: undefined as any,
    };
  }
}
