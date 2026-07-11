import { Component, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

type VerifyState = 'verifying' | 'success' | 'error' | 'missing-token';

@Component({
  selector: 'app-verify-email',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './verify-email.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './verify-email.scss',
})
export class VerifyEmail implements OnInit {
  state: VerifyState = 'verifying';
  errorMessage = '';

  // Resend form (shown once verification fails, e.g. an expired 24h token)
  resendForm;
  resendLoading = false;
  resendSubmitted = false;
  resendError = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
  ) {
    this.resendForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.state = 'missing-token';
      return;
    }

    this.authService.verifyEmail({ token }).subscribe({
      next: () => (this.state = 'success'),
      error: (err) => {
        this.state = 'error';
        this.errorMessage = err?.error?.message || 'This verification link is invalid or has expired.';
      },
    });
  }

  resend(): void {
    if (this.resendForm.invalid) {
      this.resendForm.markAllAsTouched();
      return;
    }
    this.resendLoading = true;
    this.resendError = '';

    this.authService.resendVerification(this.resendForm.getRawValue() as any).subscribe({
      // Backend always returns 200 regardless of whether the email exists / is already verified.
      next: () => {
        this.resendLoading = false;
        this.resendSubmitted = true;
      },
      error: (err) => {
        this.resendError = err?.error?.message || 'Something went wrong. Please try again.';
        this.resendLoading = false;
      },
    });
  }
}
