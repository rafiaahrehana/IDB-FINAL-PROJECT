import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from './api.service';
import {
  AiProviderConfig,
  AiProviderConfigRequest,
  AiUsageSummary,
  AiPromptTemplate,
  AiPromptTemplateRequest,
} from '../../modules/ai/models/ai.model';

export interface AiGenerateResponse {
  conversationUuid: string;
  feature: string;
  provider: string;
  model: string;
  result: string;
  executionTimeMs: number;
}

@Injectable({ providedIn: 'root' })
export class AiService {
  constructor(private api: ApiService) {}

  generate(feature: string, prompt: string): Observable<AiGenerateResponse> {
    return this.api.post<AiGenerateResponse>('/ai/generate', { feature, prompt });
  }

  conversations(feature?: string, page = 0): Observable<PagedResponse<AiGenerateResponse>> {
    return this.api.getPaged<AiGenerateResponse>('/ai/conversations', page, 20, feature ? { feature } : undefined);
  }

  getConfig(): Observable<AiProviderConfig> {
    return this.api.get<AiProviderConfig>('/ai/config');
  }

  saveConfig(config: AiProviderConfigRequest): Observable<AiProviderConfig> {
    return this.api.post<AiProviderConfig>('/ai/config', config);
  }

  getUsage(date?: string): Observable<AiUsageSummary> {
    return this.api.get<AiUsageSummary>('/ai/usage', date ? { date } : undefined);
  }

  listTemplates(page = 0, size = 20): Observable<PagedResponse<AiPromptTemplate>> {
    return this.api.getPaged<AiPromptTemplate>('/ai/templates', page, size);
  }

  saveTemplate(template: AiPromptTemplateRequest): Observable<AiPromptTemplate> {
    return this.api.post<AiPromptTemplate>('/ai/templates', template);
  }
}
