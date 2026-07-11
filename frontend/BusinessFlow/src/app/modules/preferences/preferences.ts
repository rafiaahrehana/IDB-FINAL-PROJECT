import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  NotificationPreference,
  UpdateNotificationPreferenceRequest,
} from '../../core/models/notification.model';
import { NotificationService } from '../../core/services/notification.service';
import { Loader } from '../../shared/components/loader/loader';
import { AuthService } from '../../core/services/auth.service';
import { LocationComponent } from '../../shared/components/location/location.component';
import { LocationRequest } from '../../shared/models/location.model';

interface PrefRow {
  key: keyof UpdateNotificationPreferenceRequest;
  label: string;
  description: string;
  channel: 'Email' | 'In-app';
}

@Component({
  selector: 'app-notification-preferences',
  imports: [CommonModule, FormsModule, Loader, LocationComponent],
  templateUrl: './preferences.html',
  styleUrl: './preferences.scss'
})
export class Preferences implements OnInit {
  @ViewChild(LocationComponent) locationComponent!: LocationComponent;

  activeTab: 'profile' | 'notifications' | 'security' = 'profile';

  // PROFILE VARIABLES
  profile: any = null;
  loadingProfile = false;
  savingProfile = false;

  profileForm = {
    firstName: '',
    lastName: '',
    phone: '',
    image: '',
    languagePreference: 'EN'
  };

  // SECURITY (CHANGE PASSWORD) VARIABLES
  changingPassword = false;

  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  // NOTIFICATION VARIABLES
  preference: NotificationPreference | null = null;
  loading = false;
  saving = false;
  error = '';
  success = '';

  // GROUPED ROWS FOR THE UI
  emailRows: PrefRow[] = [
    { key: 'emailOnServiceRequest', label: 'Service requests', description: 'New service requests created or received', channel: 'Email' },
    { key: 'emailOnStatusChange', label: 'Status changes', description: 'Updates on service request status transitions', channel: 'Email' },
    { key: 'emailOnInvoice', label: 'Invoices', description: 'New invoices generated for you', channel: 'Email' },
    { key: 'emailOnPayment', label: 'Payments', description: 'Payments received or processed', channel: 'Email' },
    { key: 'emailOnTaskAssigned', label: 'Task assignments', description: 'Tasks or requests assigned to you', channel: 'Email' },
    { key: 'emailOnLeaveUpdate', label: 'Leave updates', description: 'Leave request approvals or rejections', channel: 'Email' },
    { key: 'emailMarketing', label: 'Product updates', description: 'Occasional product news and announcements', channel: 'Email' },
  ];

  inAppRows: PrefRow[] = [
    { key: 'inAppOnServiceRequest', label: 'Service requests', description: 'New service requests', channel: 'In-app' },
    { key: 'inAppOnStatusChange', label: 'Status changes', description: 'Status transition updates', channel: 'In-app' },
  ];

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.load();
  }

  // TAB CONTROLS
  setTab(tab: 'profile' | 'notifications' | 'security'): void {
    this.activeTab = tab;
    this.error = '';
    this.success = '';
    
    if (tab === 'profile' && this.profile && this.profile.location) {
      setTimeout(() => {
        if (this.locationComponent) {
          this.locationComponent.resolveExistingLocation(this.profile.location);
        }
      }, 50);
    }
  }

  // LOAD PROFILE
  loadProfile(): void {
    this.loadingProfile = true;
    this.error = '';
    this.authService.getProfile().subscribe({
      next: (res) => {
        this.profile = res;
        this.profileForm = {
          firstName: res.firstName || '',
          lastName: res.lastName || '',
          phone: res.phone || '',
          image: res.image || '',
          languagePreference: res.languagePreference || 'EN'
        };
        this.loadingProfile = false;
        
        if (res.location) {
          setTimeout(() => {
            if (this.locationComponent) {
              this.locationComponent.resolveExistingLocation(res.location);
            }
          }, 50);
        }
      },
      error: () => {
        this.error = 'Failed to load profile details';
        this.loadingProfile = false;
      }
    });
  }

  // SAVE PROFILE
  saveProfile(locationData?: LocationRequest): void {
    if (!this.profileForm.firstName.trim() || !this.profileForm.lastName.trim()) {
      this.error = 'First name and Last name are required';
      return;
    }

    this.savingProfile = true;
    this.error = '';
    this.success = '';

    const payload: any = {
      firstName: this.profileForm.firstName.trim(),
      lastName: this.profileForm.lastName.trim(),
      phone: this.profileForm.phone ? this.profileForm.phone.trim() : null,
      image: this.profileForm.image ? this.profileForm.image.trim() : null,
      languagePreference: this.profileForm.languagePreference
    };

    if (locationData) {
      payload.location = locationData;
    } else if (this.locationComponent) {
      if (this.locationComponent.locationForm.valid) {
        payload.location = this.locationComponent.locationForm.getRawValue();
      }
    }

    this.authService.updateProfile(payload).subscribe({
      next: (res) => {
        this.profile = res;
        this.success = 'Profile and location updated successfully';
        this.savingProfile = false;
        if (res.location && this.locationComponent) {
          this.locationComponent.resolveExistingLocation(res.location);
        }
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update profile details';
        this.savingProfile = false;
      }
    });
  }

  // CHANGE PASSWORD
  changePassword(): void {
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm;

    if (!currentPassword || !newPassword || !confirmPassword) {
      this.error = 'All password fields are required';
      return;
    }
    if (newPassword.length < 8) {
      this.error = 'New password must be at least 8 characters';
      return;
    }
    if (newPassword !== confirmPassword) {
      this.error = 'New passwords do not match';
      return;
    }

    this.changingPassword = true;
    this.error = '';
    this.success = '';

    this.authService.changePassword({ currentPassword, newPassword, confirmPassword }).subscribe({
      next: () => {
        this.changingPassword = false;
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        // Backend revokes ALL refresh tokens on password change, so this session
        // can no longer refresh - log out and send the user back to sign in.
        this.success = 'Password changed successfully. Redirecting to sign in...';
        setTimeout(() => this.authService.logout(), 2000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to change password';
        this.changingPassword = false;
      }
    });
  }

  // LOAD PREFERENCES
  load(): void {
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.notificationService.getPreferences().subscribe({
      next: (res: NotificationPreference) => { this.preference = res; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Failed to load preferences'; this.loading = false; this.cdr.markForCheck(); }
    });
  }

  // SAVE PREFERENCES
  save(): void {
    if (!this.preference) return;
    this.saving = true;
    this.cdr.markForCheck();
    this.error = '';
    this.success = '';
    const payload: UpdateNotificationPreferenceRequest = {
      emailOnServiceRequest: this.preference.emailOnServiceRequest,
      emailOnStatusChange: this.preference.emailOnStatusChange,
      emailOnInvoice: this.preference.emailOnInvoice,
      emailOnPayment: this.preference.emailOnPayment,
      emailOnTaskAssigned: this.preference.emailOnTaskAssigned,
      emailOnLeaveUpdate: this.preference.emailOnLeaveUpdate,
      inAppOnServiceRequest: this.preference.inAppOnServiceRequest,
      inAppOnStatusChange: this.preference.inAppOnStatusChange,
      emailMarketing: this.preference.emailMarketing,
    };
    this.notificationService.updatePreferences(payload).subscribe({
      next: (res: NotificationPreference) => { this.preference = res; this.saving = false; this.cdr.markForCheck(); this.success = 'Preferences saved'; },
      error: (err: any) => { this.saving = false; this.cdr.markForCheck(); this.error = err?.error?.message || 'Failed to save preferences'; }
    });
  }

  // RESET TO DEFAULTS
  reset(): void {
    this.notificationService.resetPreferences().subscribe({
      next: (res: NotificationPreference) => { this.preference = res; this.success = 'Preferences reset to defaults'; },
      error: (err: any) => this.error = err?.error?.message || 'Failed to reset preferences'
    });
  }
}
