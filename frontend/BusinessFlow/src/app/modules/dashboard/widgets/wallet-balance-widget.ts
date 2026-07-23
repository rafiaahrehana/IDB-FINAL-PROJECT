import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { DashboardSummary } from '../../../core/services/dashboard.service';
import { StatCard } from '../../../shared/components/stat-card/stat-card';

@Component({
  selector: 'app-wallet-balance-widget',
  imports: [StatCard, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-stat-card label="Wallet Balance" [value]="summary.walletBalance | currency:'BDT ':'symbol':'1.0-0'"
      icon="bi-wallet2" variant="dark" link="/finance/wallet" />
  `,
})
export class WalletBalanceWidget {
  @Input({ required: true }) summary!: DashboardSummary;
}
