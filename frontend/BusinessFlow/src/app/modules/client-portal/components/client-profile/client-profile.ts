import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../crm/services/client.service';
import { Client } from '../../../crm/models/crm.model';
import { Loader } from '../../../../shared/components/loader/loader';
import { FileUpload } from '../../../../shared/components/file-upload/file-upload';
import { FileUploadResult } from '../../../../shared/services/file-upload.service';

@Component({
  selector: 'app-client-profile',
  imports: [CommonModule, FormsModule, Loader, FileUpload],
  templateUrl: './client-profile.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientProfile implements OnInit {
  loading = true;
  client?: Client;

  // Personal info (User)
  personalForm: any = { firstName: '', lastName: '', phone: '', image: '' };
  savingPersonal = false;
  personalError = '';
  personalSuccess = '';

  // Company info (Client)
  companyForm: any = { clientCompanyName: '', industry: '', website: '', billingAddress: '', shippingAddress: '' };
  savingCompany = false;
  companyError = '';
  companySuccess = '';

  // Password
  passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  savingPassword = false;
  passwordError = '';
  passwordSuccess = '';

  constructor(
    private auth: AuthService,
    private clientService: ClientService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.auth.getProfile().subscribe({
      next: (p: any) => {
        this.personalForm = { firstName: p.firstName || '', lastName: p.lastName || '', phone: p.phone || '', image: p.image || '' };
        this.cdr.markForCheck();
      },
    });
    this.clientService.getMyProfile().subscribe({
      next: (c) => {
        this.client = c;
        this.companyForm = {
          clientCompanyName: c.clientCompanyName || '',
          industry: c.industry || '',
          website: c.website || '',
          billingAddress: c.billingAddress || '',
          shippingAddress: c.shippingAddress || '',
        };
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); },
    });
  }

  onAvatarUploaded(result: FileUploadResult): void {
    this.personalForm.image = result.fileUrl;
  }

  savePersonal(): void {
    this.savingPersonal = true;
    this.personalError = '';
    this.personalSuccess = '';
    this.cdr.markForCheck();
    this.auth.updateProfile(this.personalForm).subscribe({
      next: () => {
        this.personalSuccess = 'Profile updated';
        this.savingPersonal = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.personalError = err?.error?.message || 'Failed to update profile';
        this.savingPersonal = false;
        this.cdr.markForCheck();
      },
    });
  }

  saveCompany(): void {
    this.savingCompany = true;
    this.companyError = '';
    this.companySuccess = '';
    this.cdr.markForCheck();
    this.clientService.updateMyProfile(this.companyForm).subscribe({
      next: (c) => {
        this.client = c;
        this.companySuccess = 'Company details updated';
        this.savingCompany = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.companyError = err?.error?.message || 'Failed to update company details';
        this.savingCompany = false;
        this.cdr.markForCheck();
      },
    });
  }

  changePassword(): void {
    if (!this.passwordForm.currentPassword || !this.passwordForm.newPassword) {
      this.passwordError = 'Current and new password are required';
      this.cdr.markForCheck();
      return;
    }
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.passwordError = 'New password and confirmation do not match';
      this.cdr.markForCheck();
      return;
    }
    this.savingPassword = true;
    this.passwordError = '';
    this.passwordSuccess = '';
    this.cdr.markForCheck();
    this.auth.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.passwordSuccess = 'Password changed. Please sign in again next time with your new password.';
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        this.savingPassword = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.passwordError = err?.error?.message || 'Failed to change password';
        this.savingPassword = false;
        this.cdr.markForCheck();
      },
    });
  }
}
