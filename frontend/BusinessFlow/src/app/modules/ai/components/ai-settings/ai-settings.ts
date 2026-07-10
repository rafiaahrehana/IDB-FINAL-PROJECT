import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../../../core/services/ai.service';
import {
  AiProviderType,
  AiModel,
  AiFeature,
  AiProviderConfig,
  AiProviderConfigRequest,
  AiUsageSummary,
  AiPromptTemplate,
  AiPromptTemplateRequest,
} from '../../models/ai.model';

@Component({
  selector: 'app-ai-settings',
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-settings.html',
})
export class AiSettings implements OnInit {
  providers: AiProviderType[] = ['GEMINI', 'CLAUDE', 'OPENAI', 'MOCK'];
  models: AiModel[] = [
    'GEMINI_2_5_FLASH',
    'GEMINI_2_5_PRO',
    'GPT_4O',
    'GPT_4O_MINI',
    'CLAUDE_SONNET',
    'CLAUDE_OPUS',
  ];
  features: AiFeature[] = [
    'EMPLOYMENT_LETTER',
    'LEAVE_POLICY',
    'PERFORMANCE_REVIEW',
    'CRM_LEAD_SUMMARY',
    'CRM_ACTIVITY_SUMMARY',
    'INVOICE_SUMMARY',
    'SERVICE_REQUEST_SUMMARY',
    'ANNOUNCEMENT_DRAFT',
    'WORKFLOW_SUGGESTION',
    'SEARCH_ANSWER',
    'BUSINESS_INSIGHTS',
    'GENERAL',
  ];

  config: AiProviderConfigRequest = {
    aiProviderType: 'GEMINI',
    model: 'GEMINI_2_5_FLASH',
    temperature: 0.7,
    maxTokens: 1024,
  };
  savingConfig = false;
  configError = '';

  usageDate = '';
  usage?: AiUsageSummary;
  loadingUsage = false;
  usageError = '';

  templates: AiPromptTemplate[] = [];
  loadingTemplates = false;
  newTemplate: AiPromptTemplateRequest = {
    feature: 'GENERAL',
    name: '',
    template: '',
    changeNotes: '',
  };
  savingTemplate = false;
  templateError = '';

  objectKeys = Object.keys;

  constructor(private aiService: AiService) {}

  ngOnInit(): void {
    this.loadConfig();
    this.loadTemplates();
  }

  loadConfig(): void {
    this.aiService.getConfig().subscribe({
      next: (res) => {
        this.config = {
          aiProviderType: res.provider,
          model: res.model,
          temperature: res.temperature,
          maxTokens: res.maxTokens,
        };
      },
      error: () => {
        /* no config yet — keep defaults */
      },
    });
  }

  saveConfig(): void {
    this.savingConfig = true;
    this.configError = '';
    this.aiService.saveConfig(this.config).subscribe({
      next: () => (this.savingConfig = false),
      error: (err) => {
        this.configError = err?.error?.message || 'Failed to save provider config';
        this.savingConfig = false;
      },
    });
  }

  loadUsage(): void {
    this.loadingUsage = true;
    this.usageError = '';
    this.aiService.getUsage(this.usageDate || undefined).subscribe({
      next: (res) => {
        this.usage = res;
        this.loadingUsage = false;
      },
      error: (err) => {
        this.usageError = err?.error?.message || 'Failed to load usage';
        this.loadingUsage = false;
      },
    });
  }

  loadTemplates(): void {
    this.loadingTemplates = true;
    this.aiService.listTemplates().subscribe({
      next: (res) => {
        this.templates = res.content;
        this.loadingTemplates = false;
      },
      error: () => (this.loadingTemplates = false),
    });
  }

  saveTemplate(): void {
    if (!this.newTemplate.name.trim() || !this.newTemplate.template.trim()) return;
    this.savingTemplate = true;
    this.templateError = '';
    this.aiService.saveTemplate(this.newTemplate).subscribe({
      next: () => {
        this.savingTemplate = false;
        this.newTemplate = { feature: 'GENERAL', name: '', template: '', changeNotes: '' };
        this.loadTemplates();
      },
      error: (err) => {
        this.templateError = err?.error?.message || 'Failed to save template';
        this.savingTemplate = false;
      },
    });
  }
}
