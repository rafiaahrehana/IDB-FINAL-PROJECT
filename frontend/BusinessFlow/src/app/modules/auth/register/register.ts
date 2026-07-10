import { Component, ChangeDetectionStrategy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/auth.model';
import { LocationComponent } from '../../../shared/components/location/location.component';
import { LocationRequest } from '../../../shared/models/location.model';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LocationComponent],
  templateUrl: './register.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './register.scss',
})
export class Register {
  @ViewChild(LocationComponent) locationComponent!: LocationComponent;

  loading = false;
  error = '';
  step = 1;

  form;
  locationValid = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      companyName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
      subdomain: ['', [Validators.required, Validators.pattern('^[a-z0-9]([a-z0-9-]{1,48}[a-z0-9])?$')]],
      companyPhone: ['', [Validators.maxLength(30)]],
    });
  }

  goStep2(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.step = 2;
    this.error = '';
  }

  goStep1(): void {
    this.step = 1;
    this.error = '';
  }

  onLocationFormReady(): void {
    if (this.locationComponent) {
      this.locationValid = this.locationComponent.locationForm.valid;
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const locationData: LocationRequest | null =
      this.locationComponent && this.locationComponent.locationForm.valid
        ? this.locationComponent.locationForm.getRawValue()
        : null;

    this.loading = true;
    this.error = '';

    const payload: RegisterRequest = {
      ...this.form.getRawValue() as RegisterRequest,
    };

    if (locationData) {
      payload.country = locationData.country;
      payload.level1 = locationData.level1;
      payload.level2 = locationData.level2;
      payload.level3 = locationData.level3;
      payload.level4 = locationData.level4;
      payload.streetAddress = locationData.streetAddress;
      payload.postalCode = locationData.postalCode;
      payload.apartment = locationData.apartment;
    }

    this.authService.register(payload).subscribe({
      next: () => this.router.navigate(['/auth/login'], { queryParams: { registered: '1' } }),
      error: (err) => {
        if (err?.error?.message) {
          this.error = err.error.message;
        } else if (err?.message) {
          this.error = err.message;
        } else {
          this.error = 'Registration failed. Please try again.';
        }
        this.loading = false;
      },
    });
  }
}
