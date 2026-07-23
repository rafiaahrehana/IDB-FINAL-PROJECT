import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { DashboardSummary } from '../../../core/services/dashboard.service';
import { StatCard } from '../../../shared/components/stat-card/stat-card';

@Component({
  selector: 'app-open-opportunities-widget',
  imports: [StatCard, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-stat-card label="Open Opportunities" [value]="summary.openOpportunities" icon="bi-kanban"
      variant="primary" [sub]="(summary.pipelineValue | currency:'BDT ':'symbol':'1.0-0') + ' pipeline'" link="/crm/pipeline" />
  `,
})
export class OpenOpportunitiesWidget {
  @Input({ required: true }) summary!: DashboardSummary;
}
