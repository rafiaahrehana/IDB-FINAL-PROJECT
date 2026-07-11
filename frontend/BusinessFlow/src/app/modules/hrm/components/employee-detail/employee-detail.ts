import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
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
import { Loader } from '../../../../shared/components/loader/loader';
import { LocationComponent } from '../../../../shared/components/location/location.component';

@Component({
  selector: 'app-employee-detail',
  imports: [CommonModule, FormsModule, RouterLink, Loader, LocationComponent],
  templateUrl: './employee-detail.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './employee-detail.scss',
})
export class EmployeeDetail implements OnInit {
  employee: Employee | null = null;
  departments: Department[] = [];
  designations: Designation[] = [];
  loading = false;
  saving = false;
  editing = false;
  error = '';
  success = '';
  form: UpdateEmployeeRequest = {};

  statuses = EMPLOYMENT_STATUSES;
  types = EMPLOYMENT_TYPES;
  genders = GENDERS;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private designationService: DesignationService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    this.departmentService.listActive().subscribe({ next: (d) => (this.departments = d) });
    this.designationService.listActive().subscribe({ next: (d) => (this.designations = d) });
  }

  load(id: number): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.employeeService.getById(id).subscribe({
      next: (emp) => {
        this.employee = emp;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load employee';
        this.loading = false;
        this.cdr.markForCheck();
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
      officialEmail: e.officialEmail,
      workPhone: e.workPhone,
      officeLocation: e.officeLocation,
      costCenter: e.costCenter,
      nationalId: e.nationalId,
      taxId: e.taxId,
      emergencyContactName: e.emergencyContactName,
      emergencyContactPhone: e.emergencyContactPhone,
      emergencyContactRelation: e.emergencyContactRelation,
    };
    if (e.location) {
      this.form.location = { ...e.location } as any;
    }
    this.editing = true;
    this.success = '';
  }

  onLocationChange(loc: any) {
    this.form.location = loc;
  }

  save(): void {
    if (!this.employee) return;
    this.saving = true;
    this.cdr.markForCheck();
    this.error = '';
    const payload: any = { ...this.form };
    Object.keys(payload).forEach((k) => {
      if (payload[k] === '' || payload[k] === null) delete payload[k];
    });
    this.employeeService.update(this.employee.id, payload).subscribe({
      next: (emp) => {
        this.employee = emp;
        this.saving = false;
        this.cdr.markForCheck();
        this.editing = false;
        this.success = 'Employee updated successfully';
      },
      error: (err) => {
        this.saving = false;
        this.cdr.markForCheck();
        this.error = err?.error?.message || 'Failed to update employee';
      },
    });
  }
}
