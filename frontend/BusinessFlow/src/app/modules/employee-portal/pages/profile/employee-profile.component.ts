import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeePortalService, EmployeeProfile } from '../../services/employee-portal.service';

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <div>
        <h4 class="page-title">My Profile</h4>
        <p class="page-subtitle">View and update your personal information.</p>
      </div>
    </div>

    @if (loading) {
      <div class="text-center py-5"><div class="spinner-border text-primary"></div></div>
    } @else if (profile) {
      <!-- Profile Header -->
      <div class="card border-0 shadow-sm p-4 mb-4">
        <div class="d-flex align-items-center gap-4">
          <div class="avatar-lg">
            @if (profile.profileImageUrl) {
              <img [src]="profile.profileImageUrl" class="rounded-circle" width="100" height="100" style="object-fit: cover">
            } @else {
              <div class="avatar-placeholder">{{ profile.firstName?.charAt(0) }}</div>
            }
          </div>
          <div>
            <h4 class="fw-bold mb-1">{{ profile.firstName }} {{ profile.lastName }}</h4>
            <p class="text-muted mb-1">{{ profile.jobTitle || 'Employee' }} &middot; {{ profile.departmentName || 'N/A' }}</p>
            <p class="text-muted mb-0 small">Employee ID: {{ profile.employeeNumber || 'N/A' }}</p>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <ul class="nav nav-tabs mb-4">
        <li class="nav-item"><a class="nav-link" [class.active]="tab === 'personal'" (click)="tab = 'personal'">Personal</a></li>
        <li class="nav-item"><a class="nav-link" [class.active]="tab === 'employment'" (click)="tab = 'employment'">Employment</a></li>
        <li class="nav-item"><a class="nav-link" [class.active]="tab === 'salary'" (click)="tab = 'salary'">Salary</a></li>
        <li class="nav-item"><a class="nav-link" [class.active]="tab === 'emergency'" (click)="tab = 'emergency'">Emergency</a></li>
      </ul>

      <!-- Personal Tab -->
      @if (tab === 'personal') {
        <div class="card border-0 shadow-sm p-4">
          <h6 class="fw-bold mb-3">Personal Information</h6>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">First Name</label>
              <input type="text" class="form-control" [(ngModel)]="editData.firstName">
            </div>
            <div class="col-md-6">
              <label class="form-label">Last Name</label>
              <input type="text" class="form-control" [(ngModel)]="editData.lastName">
            </div>
            <div class="col-md-6">
              <label class="form-label">Email</label>
              <input type="email" class="form-control" [value]="profile.email" disabled>
            </div>
            <div class="col-md-6">
              <label class="form-label">Phone</label>
              <input type="tel" class="form-control" [(ngModel)]="editData.phone">
            </div>
            <div class="col-md-6">
              <label class="form-label">Date of Birth</label>
              <input type="text" class="form-control" [value]="profile.dateOfBirth || 'N/A'" disabled>
            </div>
            <div class="col-md-6">
              <label class="form-label">Gender</label>
              <input type="text" class="form-control" [value]="profile.gender || 'N/A'" disabled>
            </div>
            <div class="col-md-6">
              <label class="form-label">National ID</label>
              <input type="text" class="form-control" [value]="profile.nationalId || 'N/A'" disabled>
            </div>
            <div class="col-md-6">
              <label class="form-label">Office Location</label>
              <input type="text" class="form-control" [value]="profile.officeLocation || 'N/A'" disabled>
            </div>
          </div>
          <button class="btn btn-primary mt-3" (click)="saveProfile()" [disabled]="saving">
            @if (saving) { <span class="spinner-border spinner-border-sm me-1"></span> }
            Save Changes
          </button>
          @if (saveMsg) { <span class="text-success ms-3">{{ saveMsg }}</span> }
        </div>
      }

      <!-- Employment Tab -->
      @if (tab === 'employment') {
        <div class="card border-0 shadow-sm p-4">
          <h6 class="fw-bold mb-3">Employment Details</h6>
          <div class="row g-3">
            <div class="col-md-4"><small class="text-muted d-block">Employee ID</small><strong>{{ profile.employeeNumber }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Job Title</small><strong>{{ profile.jobTitle || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Employment Type</small><strong>{{ profile.employmentType }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Department</small><strong>{{ profile.departmentName || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Designation</small><strong>{{ profile.designationName || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Reporting Manager</small><strong>{{ profile.reportingManagerName || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Hire Date</small><strong>{{ profile.hireDate | date:'mediumDate' }}</strong></div>
                <div class="col-md-4"><small class="text-muted d-block">Confirmation Date</small><strong>{{ profile.confirmationDate ? (profile.confirmationDate | date:'mediumDate') : '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Employment Status</small><strong class="text-success">{{ profile.employmentStatus }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Shift</small><strong>{{ profile.shiftName || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Cost Center</small><strong>{{ profile.costCenter || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Official Email</small><strong>{{ profile.officialEmail || '--' }}</strong></div>
          </div>
        </div>
      }

      <!-- Salary Tab -->
      @if (tab === 'salary') {
        <div class="card border-0 shadow-sm p-4">
          <h6 class="fw-bold mb-3">Salary Information</h6>
          <div class="row g-4">
            <div class="col-md-3">
              <div class="salary-card p-3 rounded text-center">
                <small class="text-muted">Basic Salary</small>
                <h5 class="fw-bold mt-1 mb-0">{{ profile.basicSalary | currency }}</h5>
              </div>
            </div>
            <div class="col-md-3">
              <div class="salary-card p-3 rounded text-center">
                <small class="text-muted">House Rent</small>
                <h5 class="fw-bold mt-1 mb-0">{{ profile.houseRent | currency }}</h5>
              </div>
            </div>
            <div class="col-md-3">
              <div class="salary-card p-3 rounded text-center">
                <small class="text-muted">Medical Allowance</small>
                <h5 class="fw-bold mt-1 mb-0">{{ profile.medicalAllowance | currency }}</h5>
              </div>
            </div>
            <div class="col-md-3">
              <div class="salary-card p-3 rounded text-center">
                <small class="text-muted">Transport Allowance</small>
                <h5 class="fw-bold mt-1 mb-0">{{ profile.transportAllowance | currency }}</h5>
              </div>
            </div>
          </div>
          <div class="mt-3 p-3 rounded" style="background: #f8fafc">
            <small class="text-muted">Bank Name</small>
            <strong class="d-block">{{ profile.bankName || '--' }}</strong>
          </div>
        </div>
      }

      <!-- Emergency Tab -->
      @if (tab === 'emergency') {
        <div class="card border-0 shadow-sm p-4">
          <h6 class="fw-bold mb-3">Emergency Contact</h6>
          <div class="row g-3">
            <div class="col-md-4"><small class="text-muted d-block">Contact Name</small><strong>{{ profile.emergencyContactName || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Phone</small><strong>{{ profile.emergencyContactPhone || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Relation</small><strong>{{ profile.emergencyContactRelation || '--' }}</strong></div>
          </div>
          <div class="row g-3 mt-2">
            <div class="col-md-4"><small class="text-muted d-block">Tax ID</small><strong>{{ profile.taxId || '--' }}</strong></div>
            <div class="col-md-4"><small class="text-muted d-block">Work Phone</small><strong>{{ profile.workPhone || '--' }}</strong></div>
          </div>
        </div>
      }
    }
  `,
  styles: [`
    .avatar-placeholder { width: 100px; height: 100px; border-radius: 50%; background: linear-gradient(135deg, #2563EB, #7C3AED); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; font-weight: 700; }
    .salary-card { background: #f8fafc; }
    .nav-tabs .nav-link.active { color: #2563EB; border-bottom-color: #2563EB; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.25rem; }
    .page-subtitle { color: #64748b; margin: 0; }
  `]
})
export class EmployeeProfileComponent implements OnInit {
  private empService = inject(EmployeePortalService);
  profile: EmployeeProfile | null = null;
  editData = { firstName: '', lastName: '', phone: '' };
  tab = 'personal';
  loading = true;
  saving = false;
  saveMsg = '';

  ngOnInit(): void {
    this.empService.getMyProfile().subscribe(p => {
      this.profile = p;
      this.editData = { firstName: p.firstName, lastName: p.lastName, phone: p.phone || '' };
      this.loading = false;
    });
  }

  saveProfile(): void {
    this.saving = true;
    this.saveMsg = '';
    this.empService.updateUserProfile(this.editData).subscribe({
      next: () => { this.saving = false; this.saveMsg = 'Profile updated!'; },
      error: () => this.saving = false
    });
  }
}
