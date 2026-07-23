import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { Opportunity, PipelineSummary } from '../../models/crm.model';
import { OpportunityService } from '../../services/opportunity.service';
import { Loader } from '../../../../shared/components/loader/loader';

// Validated categorical/status slots (see dataviz skill palette) - fixed per
// series, never reassigned by data. Won/Lost use the reserved status colors
// since they represent an outcome's state, not an arbitrary category.
const STAGE_HUE = '#7d55fa';
const STATUS_GOOD = '#10b981';
const STATUS_CRITICAL = '#ef4444';

@Component({
  selector: 'app-pipeline-reports',
  imports: [CommonModule, RouterLink, BaseChartDirective, Loader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pipeline-reports.html',
})
export class PipelineReports implements OnInit {
  loading = false;
  error = '';

  summary?: PipelineSummary;
  wonDeals: Opportunity[] = [];
  lostDeals: Opportunity[] = [];

  stageChartData?: ChartConfiguration<'bar'>['data'];
  stageChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: { x: { beginAtZero: true } },
  };

  trendChartData?: ChartConfiguration<'bar'>['data'];
  trendChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top' } },
    scales: { y: { beginAtZero: true } },
  };

  constructor(private opportunityService: OpportunityService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    forkJoin({
      summary: this.opportunityService.pipelineSummary(),
      won: this.opportunityService.list(0, 500, { stage: 'CLOSED_WON' }),
      lost: this.opportunityService.list(0, 500, { stage: 'CLOSED_LOST' }),
    }).subscribe({
      next: ({ summary, won, lost }) => {
        this.summary = summary;
        this.wonDeals = won.content;
        this.lostDeals = lost.content;
        this.buildStageChart(summary);
        this.buildTrendChart();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load pipeline reports';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  get wonAmount(): number {
    return this.wonDeals.reduce((sum, d) => sum + (d.amount || 0), 0);
  }

  get lostAmount(): number {
    return this.lostDeals.reduce((sum, d) => sum + (d.amount || 0), 0);
  }

  get winRate(): number {
    const total = this.wonDeals.length + this.lostDeals.length;
    return total > 0 ? Math.round((this.wonDeals.length / total) * 100) : 0;
  }

  private buildStageChart(summary: PipelineSummary): void {
    this.stageChartData = {
      labels: summary.stages.map((s) => this.stageLabel(s.stage)),
      datasets: [{
        data: summary.stages.map((s) => s.totalAmount),
        backgroundColor: STAGE_HUE,
        borderRadius: 4,
        maxBarThickness: 28,
      }],
    };
  }

  // Buckets closed deals into the last 6 calendar months by their close date,
  // so "trend" reflects when a deal was actually won/lost, not just a total.
  private buildTrendChart(): void {
    const months: { key: string; label: string }[] = [];
    const now = new Date();
    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push({ key: `${d.getFullYear()}-${d.getMonth()}`, label: d.toLocaleString('en-US', { month: 'short' }) });
    }
    const bucket = (deals: Opportunity[]): number[] =>
      months.map(({ key }) =>
        deals.filter((d) => {
          if (!d.actualCloseDate) return false;
          const cd = new Date(d.actualCloseDate);
          return `${cd.getFullYear()}-${cd.getMonth()}` === key;
        }).length,
      );

    this.trendChartData = {
      labels: months.map((m) => m.label),
      datasets: [
        { label: 'Won', data: bucket(this.wonDeals), backgroundColor: STATUS_GOOD, borderRadius: 3 },
        { label: 'Lost', data: bucket(this.lostDeals), backgroundColor: STATUS_CRITICAL, borderRadius: 3 },
      ],
    };
  }

  stageLabel(stage: string): string {
    return stage.replace(/_/g, ' ');
  }
}
