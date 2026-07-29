import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  ApplicationStatus,
  APPLICATION_STATUSES,
  Department,
  Designation,
  Employee,
  EMPLOYMENT_TYPES,
  HireApplicationRequest,
  JobApplication,
  JobApplicationRequest,
  JobPosting,
} from '../../models/hrm.model';
import { RecruitmentService } from '../../services/recruitment.service';
import { JobPostingService } from '../../services/job-posting.service';
import { DepartmentService } from '../../services/department.service';
import { DesignationService } from '../../services/designation.service';
import { ShiftService } from '../../services/shift.service';
import { EmployeeService } from '../../services/employee.service';
import { Pagination } from '../../../../shared/components/pagination/pagination';
import { Loader } from '../../../../shared/components/loader/loader';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-applications',
  imports: [CommonModule, FormsModule, RouterLink, Pagination, Loader, EmptyState, ConfirmDialog, HasPermissionDirective],
  templateUrl: './applications.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Applications implements OnInit {
  // VARIABLES
  applications: JobApplication[] = [];
  jobs: JobPosting[] = [];
  totalPages = 0;
  page = 0;
  loading = false;
  error = '';
  success = '';

  statusFilter: ApplicationStatus | '' = '';

  showForm = false;
  selectedJobId: number | null = null;
  form: JobApplicationRequest = { applicantName: '', applicantEmail: '' };

  statusTarget: JobApplication | null = null;
  newStatus: ApplicationStatus = 'APPLIED';
  statusNotes = '';
  deleteTarget: JobApplication | null = null;

  statuses = APPLICATION_STATUSES;

  // HIRE MODAL
  hireTarget: JobApplication | null = null;
  hireForm: HireApplicationRequest = { password: '' };
  hireConfirmPassword = '';
  hireShowPassword = false;
  hiring = false;
  departments: Department[] = [];
  designations: Designation[] = [];
  shifts: any[] = [];
  employees: Employee[] = [];
  types = EMPLOYMENT_TYPES;

  constructor(
    private recruitmentService: RecruitmentService,
    private jobPostingService: JobPostingService,
    private departmentService: DepartmentService,
    private designationService: DesignationService,
    private shiftService: ShiftService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  // LIFECYCLE HOOKS
  ngOnInit(): void { this.load(); }

  // LOAD APPLICATIONS
  load(): void {
    this.loading = true;
    this.error = '';
    this.recruitmentService.list(this.page, 20, this.statusFilter || undefined).subscribe({
      next: (res) => { this.applications = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load applications'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // LOAD OPEN JOB POSTINGS FOR THE APPLY DROPDOWN
  loadJobs(): void {
    if (this.jobs.length) return;
    this.jobPostingService.listOpen().subscribe({
      next: (res) => { this.jobs = res; this.cdr.markForCheck(); },
      error: () => { this.jobs = []; this.cdr.markForCheck(); }
    });
  }

  // OPEN APPLY FORM
  openApply(): void {
    this.form = { applicantName: '', applicantEmail: '' };
    this.selectedJobId = null;
    this.showForm = true;
    this.loadJobs();
  }

  // SUBMIT APPLICATION
  apply(): void {
    if (!this.selectedJobId) return;
    this.recruitmentService.apply(this.selectedJobId, this.form).subscribe({
      next: () => { this.showForm = false; this.success = 'Application submitted'; this.cdr.markForCheck(); this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to submit application'; this.cdr.markForCheck(); }
    });
  }

  // OPEN STATUS CHANGE DIALOG
  openStatusChange(a: JobApplication): void {
    this.statusTarget = a;
    this.newStatus = a.status;
    this.statusNotes = a.notes || '';
  }

  // SUBMIT STATUS CHANGE
  doStatusChange(): void {
    if (!this.statusTarget) return;
    this.recruitmentService.updateStatus(this.statusTarget.id, this.newStatus, this.statusNotes).subscribe({
      next: () => { this.statusTarget = null; this.success = 'Application updated'; this.cdr.markForCheck(); this.load(); },
      error: (err) => { this.error = err?.error?.message || 'Failed to update'; this.statusTarget = null; this.cdr.markForCheck(); }
    });
  }

  // LOAD DROPDOWN DATA FOR THE HIRE FORM (ONCE)
  private loadHireLookups(): void {
    if (this.departments.length) return;
    this.departmentService.listActive().subscribe({ next: (d) => { this.departments = d; this.cdr.markForCheck(); } });
    this.designationService.listActive().subscribe({ next: (d) => { this.designations = d; this.cdr.markForCheck(); } });
    this.shiftService.listActive().subscribe({ next: (res) => { this.shifts = res; this.cdr.markForCheck(); } });
    this.employeeService.list(0, 100).subscribe({ next: (res) => { this.employees = res.content; this.cdr.markForCheck(); } });
  }

  // OPEN HIRE DIALOG
  openHire(a: JobApplication): void {
    this.hireTarget = a;
    this.hireForm = { password: '' };
    this.hireConfirmPassword = '';
    this.hireShowPassword = false;
    this.loadHireLookups();
  }

  // SUBMIT HIRE
  doHire(): void {
    if (!this.hireTarget || this.hiring) return;
    if (this.hireForm.password !== this.hireConfirmPassword) {
      this.error = 'Passwords do not match';
      this.cdr.markForCheck();
      return;
    }
    this.hiring = true;
    this.recruitmentService.hire(this.hireTarget.id, this.hireForm).subscribe({
      next: () => {
        this.hiring = false;
        this.hireTarget = null;
        this.success = 'Candidate hired — employee record created';
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.hiring = false;
        this.error = err?.error?.message || 'Failed to hire candidate';
        this.cdr.markForCheck();
      },
    });
  }

  // DELETE APPLICATION
  doDelete(): void {
    if (!this.deleteTarget) return;
    this.recruitmentService.delete(this.deleteTarget.id).subscribe({
      next: () => { this.deleteTarget = null; this.success = 'Application deleted'; this.cdr.markForCheck(); this.load(); },
      error: () => { this.deleteTarget = null; this.error = 'Cannot delete application'; this.cdr.markForCheck(); }
    });
  }

  // PAGINATION
  goToPage(p: number): void { this.page = p; this.load(); }

  // STATUS BADGE CLASS
  statusClass(status: ApplicationStatus): string {
    return {
      APPLIED: 'text-bg-secondary',
      SCREENING: 'text-bg-info',
      SHORTLISTED: 'text-bg-info',
      INTERVIEW_SCHEDULED: 'text-bg-primary',
      INTERVIEWED: 'text-bg-primary',
      OFFERED: 'text-bg-warning',
      HIRED: 'text-bg-success',
      REJECTED: 'text-bg-danger',
      WITHDRAWN: 'text-bg-secondary',
    }[status] || 'text-bg-secondary';
  }
}
