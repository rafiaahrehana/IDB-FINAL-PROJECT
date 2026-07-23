import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { DashboardSummary } from '../../../core/services/dashboard.service';
import { StatCard } from '../../../shared/components/stat-card/stat-card';

@Component({
  selector: 'app-outstanding-invoices-widget',
  imports: [StatCard, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-stat-card label="Outstanding Invoices" [value]="summary.outstandingInvoiceAmount | currency:'BDT ':'symbol':'1.0-0'"
      icon="bi-file-earmark-text" [variant]="summary.outstandingInvoiceAmount > 0 ? 'danger' : 'success'" link="/finance/invoices" />
  `,
})
export class OutstandingInvoicesWidget {
  @Input({ required: true }) summary!: DashboardSummary;
}
