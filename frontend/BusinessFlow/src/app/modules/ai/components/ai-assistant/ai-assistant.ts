import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService, AiGenerateResponse } from '../../../../core/services/ai.service';
import { Loader } from '../../../../shared/components/loader/loader';

@Component({
  selector: 'app-ai-assistant',
  imports: [CommonModule, FormsModule, Loader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ai-assistant.html',
})
export class AiAssistant implements OnInit {
  features = [
    { value: 'GENERAL', label: 'General Assistant' },
    { value: 'CRM_LEAD_SUMMARY', label: 'CRM Lead Summary' },
    { value: 'CRM_ACTIVITY_SUMMARY', label: 'CRM Activity Summary' },
    { value: 'INVOICE_SUMMARY', label: 'Invoice Summary' },
    { value: 'SERVICE_REQUEST_SUMMARY', label: 'Service Request Summary' },
    { value: 'EMPLOYMENT_LETTER', label: 'Employment Letter' },
    { value: 'LEAVE_POLICY', label: 'Leave Policy Draft' },
    { value: 'PERFORMANCE_REVIEW', label: 'Performance Review Draft' },
    { value: 'ANNOUNCEMENT_DRAFT', label: 'Announcement Draft' },
    { value: 'WORKFLOW_SUGGESTION', label: 'Workflow Suggestion' },
  ];
  feature = 'GENERAL';
  prompt = '';
  result?: AiGenerateResponse;
  history: AiGenerateResponse[] = [];
  generating = false;
  loadingHistory = false;
  error = '';

  constructor(private aiService: AiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  generate(): void {
    if (!this.prompt.trim()) return;
    this.generating = true;
    this.error = '';
    this.cdr.markForCheck();
    this.aiService.generate(this.feature, this.prompt.trim()).subscribe({
      next: (res) => {
        this.result = res;
        this.generating = false;
        this.loadHistory();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error =
          err?.error?.message || 'Generation failed — check your AI provider configuration';
        this.generating = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadHistory(): void {
    this.loadingHistory = true;
    this.cdr.markForCheck();
    this.aiService.conversations().subscribe({
      next: (res) => {
        this.history = res.content;
        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
    });
  }

  reuse(item: AiGenerateResponse): void {
    this.feature = item.feature;
    this.result = item;
    this.cdr.markForCheck();
  }
}
