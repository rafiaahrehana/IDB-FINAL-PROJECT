import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {
  loading = false;
  submitted = false;
  error = '';
  form;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.cdr.markForCheck();
    this.error = '';
    this.cdr.markForCheck();

    this.authService.forgotPassword(this.form.getRawValue() as any).subscribe({
      // Backend always returns 200 regardless of whether the email exists, to avoid
      // leaking which accounts are registered - so we always show the same message.
      next: () => {
        this.loading = false;
        this.submitted = true;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Something went wrong. Please try again.';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }
}
