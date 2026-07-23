import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { DashboardSummary } from '../../../core/services/dashboard.service';
import { StatCard } from '../../../shared/components/stat-card/stat-card';

@Component({
  selector: 'app-wallet-credits-widget',
  imports: [StatCard, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-stat-card label="Wallet Credits" [value]="summary.walletCreditBalance | currency:'BDT ':'symbol':'1.0-0'"
      icon="bi-cash-stack" variant="success" link="/finance/wallet" />
  `,
})
export class WalletCreditsWidget {
  @Input({ required: true }) summary!: DashboardSummary;
}
