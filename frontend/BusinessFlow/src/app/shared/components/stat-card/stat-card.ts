import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

// Gradient variants for the stat tiles (inspired by the dashboard design reference).
export type StatCardVariant = 'primary' | 'purple' | 'success' | 'info' | 'dark' | 'danger';

@Component({
  selector: 'app-stat-card',
  imports: [RouterLink],
  templateUrl: './stat-card.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './stat-card.scss',
})
export class StatCard {
  @Input() label = '';
  @Input() value: string | number = 0;
  @Input() icon = 'bi-graph-up';
  @Input() variant: StatCardVariant = 'primary';
  // Optional caption below the value, e.g. "0 pending"
  @Input() sub = '';
  // Optional route - when set the whole card becomes a link
  @Input() link = '';
  // Optional query params for the link (e.g. pre-filtered list views)
  @Input() queryParams: Record<string, string> | null = null;
}
