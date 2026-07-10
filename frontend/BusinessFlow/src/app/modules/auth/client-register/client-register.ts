import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClientService } from '../../../modules/crm/services/client.service';
import { ClientSelfRegisterRequest } from '../../../modules/crm/models/crm.model';

@Component({
  selector: 'app-client-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './client-register.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './client-register.scss',
})
export class ClientRegister {
  loading = false;
  error = '';
  form;

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      subdomain: ['', [Validators.required, Validators.pattern('^[a-z0-9]([a-z0-9-]{1,48}[a-z0-9])?$')]],
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      clientCompanyName: ['', [Validators.maxLength(150)]],
      phone: ['', [Validators.maxLength(30)]],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    this.clientService
      .selfRegister(this.form.getRawValue() as ClientSelfRegisterRequest)
      .subscribe({
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
