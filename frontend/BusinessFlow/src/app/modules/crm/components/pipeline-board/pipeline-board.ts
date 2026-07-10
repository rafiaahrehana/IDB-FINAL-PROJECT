import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Opportunity,
  OpportunityStage,
  OPEN_STAGES,
  PipelineSummary,
} from '../../models/crm.model';
import { OpportunityService } from '../../services/opportunity.service';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-pipeline-board',
  imports: [CommonModule, FormsModule, Loader],
  templateUrl: './pipeline-board.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './pipeline-board.scss',
})
export class PipelineBoard implements OnInit {
  stages: OpportunityStage[] = [...OPEN_STAGES, 'CLOSED_WON', 'CLOSED_LOST'];
  openStages = OPEN_STAGES;
  columns: Record<string, Opportunity[]> = {};
  summary?: PipelineSummary;
  loading = false;
  error = '';
  lostReasonFor: Opportunity | null = null;
  lostReason = '';

  constructor(private opportunityService: OpportunityService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.opportunityService.list(0, 200).subscribe({
      next: (page) => {
        this.columns = {};
        this.stages.forEach((s) => (this.columns[s] = []));
        page.content.forEach((o) => (this.columns[o.stage] ??= []).push(o));
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load pipeline';
        this.loading = false;
      },
    });
    this.opportunityService.pipelineSummary().subscribe({
      next: (s) => (this.summary = s),
    });
  }

  move(opportunity: Opportunity, stage: OpportunityStage): void {
    if (stage === opportunity.stage) return;
    if (stage === 'CLOSED_LOST') {
      this.lostReasonFor = opportunity;
      this.lostReason = '';
      return;
    }
    this.opportunityService.changeStage(opportunity.id, stage).subscribe({
      next: () => this.load(),
      error: () => (this.error = 'Failed to change stage'),
    });
  }

  confirmLost(): void {
    if (!this.lostReasonFor || !this.lostReason.trim()) return;
    this.opportunityService
      .changeStage(this.lostReasonFor.id, 'CLOSED_LOST', this.lostReason.trim())
      .subscribe({
        next: () => {
          this.lostReasonFor = null;
          this.load();
        },
        error: () => (this.error = 'Failed to close opportunity'),
      });
  }

  stageLabel(stage: string): string {
    return stage.replace(/_/g, ' ');
  }

  columnTotal(stage: OpportunityStage): number {
    return (this.columns[stage] || []).reduce((sum, o) => sum + (o.amount || 0), 0);
  }
}
