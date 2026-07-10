import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import {
  Employee,
  UpdateEmployeeRequest,
  Department,
  Designation,
  EMPLOYMENT_STATUSES,
  EMPLOYMENT_TYPES,
  GENDERS,
} from '../../models/hrm.model';
import { EmployeeService } from '../../services/employee.service';
import { DepartmentService } from '../../services/department.service';
import { DesignationService } from '../../services/designation.service';
import { CustomRoleService } from '../../../platform-admin/services/custom-role.service';
import { CustomRole } from '../../../platform-admin/models/platform-admin.model';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-employee-detail',
  imports: [CommonModule, FormsModule, RouterLink, Loader],
  templateUrl: './employee-detail.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './employee-detail.scss',
})
export class EmployeeDetail implements OnInit {
  employee: Employee | null = null;
  departments: Department[] = [];
  designations: Designation[] = [];
  customRoles: CustomRole[] = [];
  loading = false;
  saving = false;
  editing = false;
  error = '';
  success = '';
  form: UpdateEmployeeRequest = {};
  selectedCustomRoleId: number | null = null;

  statuses = EMPLOYMENT_STATUSES;
  types = EMPLOYMENT_TYPES;
  genders = GENDERS;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private designationService: DesignationService,
    private customRoleService: CustomRoleService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    this.departmentService.listActive().subscribe({ next: (d) => (this.departments = d) });
    this.designationService.listActive().subscribe({ next: (d) => (this.designations = d) });
    this.customRoleService.list().subscribe({ next: (r) => (this.customRoles = r) });
  }

  load(id: number): void {
    this.loading = true;
    this.employeeService.getById(id).subscribe({
      next: (emp) => {
        this.employee = emp;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load employee';
        this.loading = false;
      },
    });
  }

  startEdit(): void {
    if (!this.employee) return;
    const e = this.employee;
    this.form = {
      jobTitle: e.jobTitle,
      designationId: e.designationId,
      employmentType: e.employmentType,
      employmentStatus: e.employmentStatus,
      gender: e.gender,
      dateOfBirth: e.dateOfBirth,
      hireDate: e.hireDate,
      departmentId: e.departmentId,
      basicSalary: e.basicSalary,
      houseRent: e.houseRent,
      medicalAllowance: e.medicalAllowance,
      transportAllowance: e.transportAllowance,
      bankName: e.bankName,
      bankAccountNumber: e.bankAccountNumber,
      officialEmail: e.officialEmail,
      workPhone: e.workPhone,
      officeLocation: e.officeLocation,
      costCenter: e.costCenter,
      nationalId: e.nationalId,
      taxId: e.taxId,
      emergencyContactName: e.emergencyContactName,
      emergencyContactPhone: e.emergencyContactPhone,
      emergencyContactRelation: e.emergencyContactRelation,
      confirmationDate: e.confirmationDate,
      probationEndDate: e.probationEndDate,
      contractEndDate: e.contractEndDate,
    };
    this.selectedCustomRoleId = e.customRoleId || null;
    this.editing = true;
    this.success = '';
  }

  save(): void {
    if (!this.employee) return;
    this.saving = true;
    this.error = '';
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null) delete payload[k];
    });
    this.employeeService.update(this.employee.id, payload).subscribe({
      next: (emp) => {
        this.employee = emp;
        this.updateCustomRole(emp.userId);
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || 'Failed to update employee';
      },
    });
  }

  private updateCustomRole(userId: number): void {
    const oldRoleId = this.employee?.customRoleId || null;
    const newRoleId = this.selectedCustomRoleId;
    if (oldRoleId === newRoleId) {
      this.saving = false;
      this.editing = false;
      this.success = 'Employee updated successfully';
      this.load(this.employee!.id);
      return;
    }
    let obs;
    if (oldRoleId && newRoleId) {
      obs = this.customRoleService.unassignFromUser(oldRoleId, userId).pipe(
        switchMap(() => this.customRoleService.assignToUser(newRoleId, userId))
      );
    } else if (oldRoleId && !newRoleId) {
      obs = this.customRoleService.unassignFromUser(oldRoleId, userId);
    } else if (!oldRoleId && newRoleId) {
      obs = this.customRoleService.assignToUser(newRoleId, userId);
    } else {
      this.saving = false;
      this.editing = false;
      this.success = 'Employee updated successfully';
      this.load(this.employee!.id);
      return;
    }
    obs.subscribe({
      next: () => {
        this.saving = false;
        this.editing = false;
        this.success = 'Employee updated successfully';
        this.load(this.employee!.id);
      },
      error: () => {
        this.saving = false;
        this.editing = false;
        this.success = 'Employee updated (role assignment failed)';
        this.load(this.employee!.id);
      },
    });
  }
}
