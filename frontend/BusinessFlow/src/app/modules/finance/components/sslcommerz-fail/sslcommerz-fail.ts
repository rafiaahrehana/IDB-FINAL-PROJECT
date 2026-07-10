import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sslcommerz-fail',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="container py-5 text-center">
      <div class="card shadow-sm mx-auto" style="max-width: 480px">
        <div class="card-body py-5">
          <div class="text-danger mb-3"><i class="bi bi-x-circle" style="font-size: 3rem"></i></div>
          <h4>Payment Failed</h4>
          <p class="text-muted">The payment could not be completed.</p>
          <p class="small text-muted">No amount was deducted from your card.</p>
          <button class="btn btn-primary mt-3" (click)="retry()">Try Again</button>
        </div>
      </div>
    </div>
  `,
})
export class SslCommerzFail {
  constructor(private router: Router) {}

  retry(): void {
    this.router.navigate(['/finance/sslcommerz/checkout']);
  }
}
