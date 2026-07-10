import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-subscription-cancel',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="container py-5 text-center">
      <div class="card shadow-sm mx-auto" style="max-width: 480px">
        <div class="card-body py-5">
          <div class="text-warning mb-3"><i class="bi bi-exclamation-triangle" style="font-size: 3rem"></i></div>
          <h4>Payment Cancelled</h4>
          <p class="text-muted">You cancelled the payment. No amount was charged.</p>
          <button class="btn btn-primary mt-3" (click)="goBack()">Back to Plans</button>
        </div>
      </div>
    </div>
  `,
})
export class SubscriptionCancel {
  constructor(private router: Router) {}
  goBack(): void { this.router.navigate(['/company/subscription']); }
}
