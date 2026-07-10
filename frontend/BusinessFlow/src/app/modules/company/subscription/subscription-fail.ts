import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-subscription-fail',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="container py-5 text-center">
      <div class="card shadow-sm mx-auto" style="max-width: 480px">
        <div class="card-body py-5">
          <div class="text-danger mb-3"><i class="bi bi-x-circle" style="font-size: 3rem"></i></div>
          <h4>Payment Failed</h4>
          <p class="text-muted">Your subscription was not activated. No amount was charged.</p>
          <button class="btn btn-primary mt-3" (click)="goBack()">Try Again</button>
        </div>
      </div>
    </div>
  `,
})
export class SubscriptionFail {
  constructor(private router: Router) {}
  goBack(): void { this.router.navigate(['/company/subscription']); }
}
