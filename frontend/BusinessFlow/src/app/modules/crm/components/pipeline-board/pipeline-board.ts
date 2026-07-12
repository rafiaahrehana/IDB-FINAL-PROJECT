import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Client,
  Opportunity,
  OpportunityStage,
  OPEN_STAGES,
  PipelineSummary,
} from '../../models/crm.model';
import { OpportunityService } from '../../services/opportunity.service';
import { ClientService } from '../../services/client.service';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-pipeline-board',
  imports: [CommonModule, FormsModule, Loader],
  templateUrl: './pipeline-board.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
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

  // Create/Edit modal state - null editing means "create"
  showForm = false;
  editing: Opportunity | null = null;
  saving = false;
  form: any = this.emptyForm();
  clients: Client[] = [];

  constructor(
    private opportunityService: OpportunityService,
    private clientService: ClientService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.load();
    // For the "Client" dropdown; a page of 100 covers typical client counts
    this.clientService.list(0, 100).subscribe({ next: (res) => { this.clients = res.content; this.cdr.markForCheck(); } });
  }

  private emptyForm(): any {
    return {
      name: '',
      clientId: null,
      description: '',
      stage: 'PROSPECTING',
      amount: null,
      probability: null,
      expectedCloseDate: '',
      nextStep: '',
    };
  }

  openCreate(): void {
    this.editing = null;
    this.form = this.emptyForm();
    this.showForm = true;
    this.cdr.markForCheck();
  }

  openEdit(deal: Opportunity): void {
    this.editing = deal;
    this.form = {
      name: deal.name,
      clientId: deal.clientId,
      description: deal.description || '',
      stage: deal.stage,
      amount: deal.amount ?? null,
      probability: deal.probability ?? null,
      expectedCloseDate: deal.expectedCloseDate || '',
      nextStep: deal.nextStep || '',
    };
    this.showForm = true;
    this.cdr.markForCheck();
  }

  save(): void {
    if (!this.form.name?.trim() || !this.form.clientId) {
      this.error = 'Name and client are required';
      this.cdr.markForCheck();
      return;
    }
    this.saving = true;
    this.error = '';
    this.cdr.markForCheck();
    const payload: any = {
      name: this.form.name.trim(),
      clientId: this.form.clientId,
      description: this.form.description || undefined,
      amount: this.form.amount ?? undefined,
      probability: this.form.probability ?? undefined,
      expectedCloseDate: this.form.expectedCloseDate || undefined,
      nextStep: this.form.nextStep || undefined,
    };
    // Stage changes on an existing deal go through the dedicated /stage endpoint
    // (it records lost reasons and timeline entries), so it's only sent on create.
    if (!this.editing) payload.stage = this.form.stage;

    const obs = this.editing
      ? this.opportunityService.update(this.editing.id, payload)
      : this.opportunityService.create(payload);
    obs.subscribe({
      next: () => {
        this.showForm = false;
        this.saving = false;
        this.cdr.markForCheck();
        this.load();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to save opportunity';
        this.saving = false;
        this.cdr.markForCheck();
      },
    });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.cdr.markForCheck();
    this.opportunityService.list(0, 200).subscribe({
      next: (page) => {
        this.columns = {};
        this.stages.forEach((s) => (this.columns[s] = []));
        page.content.forEach((o) => (this.columns[o.stage] ??= []).push(o));
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load pipeline';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
    this.opportunityService.pipelineSummary().subscribe({
      next: (s) => { this.summary = s; this.cdr.markForCheck(); },
    });
  }

  move(opportunity: Opportunity, stage: OpportunityStage): void {
    if (stage === opportunity.stage) return;
    if (stage === 'CLOSED_LOST') {
      this.lostReasonFor = opportunity;
      this.lostReason = '';
      this.cdr.markForCheck();
      return;
    }
    this.opportunityService.changeStage(opportunity.id, stage).subscribe({
      next: () => this.load(),
      error: () => { this.error = 'Failed to change stage'; this.cdr.markForCheck(); },
    });
  }

  confirmLost(): void {
    if (!this.lostReasonFor || !this.lostReason.trim()) return;
    this.opportunityService
      .changeStage(this.lostReasonFor.id, 'CLOSED_LOST', this.lostReason.trim())
      .subscribe({
        next: () => {
          this.lostReasonFor = null;
          this.cdr.markForCheck();
          this.load();
        },
        error: () => { this.error = 'Failed to close opportunity'; this.cdr.markForCheck(); },
      });
  }

  stageLabel(stage: string): string {
    return stage.replace(/_/g, ' ');
  }

  columnTotal(stage: OpportunityStage): number {
    return (this.columns[stage] || []).reduce((sum, o) => sum + (o.amount || 0), 0);
  }
}
