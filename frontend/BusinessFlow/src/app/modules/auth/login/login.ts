import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './login.scss',
})
export class Login {
  loading = false;
  error = '';
  registered = false;
  form;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {
    this.registered = this.route.snapshot.queryParamMap.get('registered') === '1';
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    this.authService.login(this.form.getRawValue() as any).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        if (err?.error?.message) {
          this.error = err.error.message;
        } else if (err?.message) {
          this.error = err.message;
        } else {
          this.error = 'Invalid username or password or server error';
        }
        this.loading = false;
      },
    });
  }
}
