import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SubscriptionService } from '../services/subscription.service';

@Component({
  selector: 'app-subscription-success',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="container py-5 text-center">
      <div class="card shadow-sm mx-auto" style="max-width: 480px">
        <div class="card-body py-5">
          @if (activating) {
            <div class="spinner-border text-primary mb-3"></div>
            <h4>Activating your subscription...</h4>
          } @else if (activated) {
            <div class="text-success mb-3"><i class="bi bi-check-circle" style="font-size: 3rem"></i></div>
            <h4>Subscription Activated!</h4>
            <p class="text-muted">Your plan is now active. Welcome to BusinessOS {{ plan }}!</p>
            <button class="btn btn-primary mt-3" (click)="goToDashboard()">Go to Dashboard</button>
          } @else {
            <div class="text-danger mb-3"><i class="bi bi-x-circle" style="font-size: 3rem"></i></div>
            <h4>Activation Failed</h4>
            <p class="text-muted">{{ error }}</p>
            <button class="btn btn-primary mt-3" (click)="goToSubscription()">Try Again</button>
          }
        </div>
      </div>
    </div>
  `,
})
export class SubscriptionSuccess implements OnInit {
  activating = true;
  activated = false;
  error = '';
  plan = '';
  tranId = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private subService: SubscriptionService,
  ) {}

  ngOnInit(): void {
    this.tranId = this.route.snapshot.queryParamMap.get('tran_id') || '';
    if (this.tranId) {
      this.subService.activate(this.tranId).subscribe({
        next: (res) => {
          this.activating = false;
          this.activated = true;
        },
        error: (err) => {
          this.activating = false;
          this.error = err?.error?.message || 'Failed to activate subscription';
        },
      });
    } else {
      this.activating = false;
      this.error = 'No transaction ID found';
    }
  }

  goToDashboard(): void { this.router.navigate(['/dashboard']); }
  goToSubscription(): void { this.router.navigate(['/company/subscription']); }
}
