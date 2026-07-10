import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SslCommerzService } from '../../services/sslcommerz.service';

@Component({
  selector: 'app-sslcommerz-success',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div class="container py-5 text-center">
      <div class="card shadow-sm mx-auto" style="max-width: 480px">
        <div class="card-body py-5">
          <div class="text-success mb-3"><i class="bi bi-check-circle" style="font-size: 3rem"></i></div>
          <h4>Payment Successful!</h4>
          @if (tranId) {
            <p class="text-muted">Transaction ID: <code>{{ tranId }}</code></p>
          }
          <p class="small text-muted">Your wallet will be credited shortly.</p>
          <button class="btn btn-primary mt-3" (click)="goToWallet()">Go to Wallet</button>
        </div>
      </div>
    </div>
  `,
})
export class SslCommerzSuccess implements OnInit {
  tranId = '';
  constructor(private route: ActivatedRoute, private router: Router, private sslService: SslCommerzService) {}

  ngOnInit(): void {
    this.tranId = this.route.snapshot.queryParamMap.get('tran_id') || '';
    if (this.tranId) {
      this.sslService.getStatus(this.tranId).subscribe();
    }
  }

  goToWallet(): void {
    this.router.navigate(['/finance/wallet']);
  }
}
