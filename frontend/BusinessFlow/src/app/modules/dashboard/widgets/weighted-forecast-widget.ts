import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { DashboardSummary } from '../../../core/services/dashboard.service';
import { StatCard } from '../../../shared/components/stat-card/stat-card';

@Component({
  selector: 'app-weighted-forecast-widget',
  imports: [StatCard, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-stat-card label="Weighted Forecast" [value]="summary.weightedForecast | currency:'BDT ':'symbol':'1.0-0'"
      icon="bi-graph-up-arrow" variant="success" link="/crm/pipeline" />
  `,
})
export class WeightedForecastWidget {
  @Input({ required: true }) summary!: DashboardSummary;
}
