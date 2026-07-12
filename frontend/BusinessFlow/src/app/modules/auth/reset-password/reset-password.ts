import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password && confirmPassword && password !== confirmPassword
    ? { passwordMismatch: true }
    : null;
}

@Component({
  selector: 'app-reset-password',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './reset-password.scss',
})
export class ResetPassword {
  loading = false;
  submitted = false;
  error = '';
  token: string | null = null;
  form;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {
    this.token = this.route.snapshot.queryParamMap.get('token');
    this.form = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordsMatchValidator },
    );

    if (!this.token) {
      this.error = 'This reset link is missing its token. Please request a new one.';
    }
  }

  submit(): void {
    if (!this.token || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.cdr.markForCheck();

    this.authService
      .resetPassword({ token: this.token, ...this.form.getRawValue() } as any)
      .subscribe({
        next: () => {
          this.loading = false;
          this.submitted = true;
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.error = err?.error?.message || 'This link is invalid or has expired. Please request a new one.';
          this.loading = false;
          this.cdr.markForCheck();
        },
      });
  }
}
