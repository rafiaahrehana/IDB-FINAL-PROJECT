import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CompanyService, CompanyResponse } from '../services/company.service';

@Component({
  selector: 'app-company-settings',
  imports: [CommonModule],
  templateUrl: './company-settings.html',
})
export class CompanySettings implements OnInit {
  company?: CompanyResponse;
  loading = false;
  error = '';
  success = '';
  showDeactivateModal = false;
  showDeleteModal = false;

  constructor(
    private companyService: CompanyService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadCompany();
  }

  loadCompany(): void {
    this.loading = true;
    this.companyService.getMyCompany().subscribe({
      next: (company) => {
        this.company = company;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load company details';
        this.loading = false;
      },
    });
  }

  openDeactivateModal(): void {
    this.showDeactivateModal = true;
  }

  closeDeactivateModal(): void {
    this.showDeactivateModal = false;
  }

  deactivateCompany(): void {
    this.companyService.deactivateMyCompany().subscribe({
      next: () => {
        this.success = 'Company deactivated successfully';
        this.showDeactivateModal = false;
        this.loadCompany();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to deactivate company';
        this.showDeactivateModal = false;
      },
    });
  }

  openDeleteModal(): void {
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
  }

  deleteCompany(): void {
    this.companyService.deleteMyCompany().subscribe({
      next: () => {
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete company';
        this.showDeleteModal = false;
      },
    });
  }

  canDeactivate(): boolean {
    return this.company?.status !== 'DEACTIVATED' && !!this.company?.active;
  }

  canDelete(): boolean {
    return this.company?.status !== 'DEACTIVATED' || this.company?.active;
  }

  statusBadgeClass(): string {
    if (!this.company) return 'bg-secondary';
    switch (this.company.status) {
      case 'ACTIVE': return 'bg-success';
      case 'SUSPENDED': return 'bg-warning';
      case 'DEACTIVATED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
