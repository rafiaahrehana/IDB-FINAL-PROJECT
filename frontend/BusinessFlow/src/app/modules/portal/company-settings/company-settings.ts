import { Component, OnInit, ChangeDetectionStrategy, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MyCompany, PortalService, UpdateCompanyRequest } from '../portal.service';
import { Loader } from '../../../shared/components/loader/loader';
import { FileUpload } from '../../../shared/components/file-upload/file-upload';
import { FileUploadResult } from '../../../shared/services/file-upload.service';
import { LocationComponent } from '../../../shared/components/location/location.component';
import { LocationRequest } from '../../../shared/models/location.model';

@Component({
  selector: 'app-company-settings',
  imports: [CommonModule, FormsModule, RouterLink, Loader, FileUpload, LocationComponent],
  templateUrl: './company-settings.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './company-settings.scss',
})
export class CompanySettings implements OnInit {
  @ViewChild(LocationComponent) locationComponent?: LocationComponent;
  company?: MyCompany;
  loading = false;
  saving = false;
  error = '';
  success = '';

  form: UpdateCompanyRequest = {};

  constructor(private portalService: PortalService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.portalService.getMyCompany().subscribe({
      next: (c) => {
        this.company = c;
        this.form = {
          companyName: c.companyName,
          companyPhone: c.companyPhone,
          website: c.website,
          location: c.location,
          logo: c.logo,
          primaryColor: c.primaryColor || '#4f46e5',
          secondaryColor: c.secondaryColor || '#7c3aed',
          tagline: c.tagline,
          portalAbout: c.portalAbout,
        };
        this.cdr.markForCheck();
        this.loading = false;
        // Prefill the structured address picker with the saved location
        if (c.locationDetail) {
          setTimeout(() => this.locationComponent?.resolveExistingLocation(c.locationDetail!));
        }
      },
      error: () => {
        this.error = 'Failed to load company';
        this.loading = false;
      },
    });
  }

  save(): void {
    if (!this.form.companyName?.trim()) {
      this.error = 'Company name is required';
      return;
    }
    this.saving = true;
    this.error = '';
    this.success = '';
    this.portalService.updateMyCompany(this.form).subscribe({
      next: (c) => {
        this.company = c;
        this.success = 'Portal settings saved';
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save settings';
        this.saving = false;
      },
    });
  }

  // The location picker emits a full LocationRequest on its own submit,
  // saved immediately (matching the preferences page flow)
  saveLocation(location: LocationRequest): void {
    this.saving = true;
    this.error = '';
    this.portalService.updateMyCompany({ locationDetail: location }).subscribe({
      next: (c) => {
        this.company = c;
        this.success = 'Company address saved';
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save address';
        this.saving = false;
      },
    });
  }

  // Uploaded via the shared FileUpload component (POST /api/upload);
  // the returned public /uploads/... URL becomes the portal logo
  onLogoUploaded(result: FileUploadResult): void {
    this.form.logo = result.fileUrl;
  }

  // Live preview of the portal hero using the colors being edited
  get heroStyle(): Record<string, string> {
    return {
      background: `linear-gradient(135deg, ${this.form.primaryColor || '#4f46e5'} 0%, ${this.form.secondaryColor || '#7c3aed'} 100%)`,
    };
  }
}
