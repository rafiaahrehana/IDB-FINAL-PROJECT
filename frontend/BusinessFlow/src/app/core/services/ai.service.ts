import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, PagedResponse } from './api.service';
import {
  AiProviderConfig,
  AiProviderConfigRequest,
  AiUsageSummary,
  AiPromptTemplate,
  AiPromptTemplateRequest,
  PageResponse
} from '../../modules/ai/models/ai.model';

export interface AiGenerateResponse {
  conversationUuid: string;
  feature: string;
  provider: string;
  model: string;
  result: string;
  executionTimeMs: number;
}

export interface SearchResultItem {
  type: string;
  id: number;
  title: string;
  subtitle: string;
  link: string;
}

export interface GlobalSearchResponse {
  query: string;
  results: SearchResultItem[];
  totalMatches: number;
}

export interface AskResponse {
  question: string;
  answer: string;
  sources: SearchResultItem[];
}

export interface Recommendation {
  type: string;
  severity: string;
  message: string;
  link: string;
}

export interface InsightsResponse {
  insights: string;
  generatedInMs: number;
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

  search(q: string): Observable<GlobalSearchResponse> {
    return this.api.get<GlobalSearchResponse>('/search', { q });
  }

  ask(question: string): Observable<AskResponse> {
    return this.api.post<AskResponse>('/search/ask', { question });
  }

  recommendations(): Observable<Recommendation[]> {
    return this.api.get<Recommendation[]>('/dashboard/recommendations');
  }

  insights(): Observable<InsightsResponse> {
    return this.api.get<InsightsResponse>('/dashboard/insights');
  }

  getConfig(): Observable<AiProviderConfig> {
    return this.api.get<AiProviderConfig>('/ai/admin/config');
  }

  saveConfig(config: AiProviderConfigRequest): Observable<AiProviderConfig> {
    return this.api.post<AiProviderConfig>('/ai/admin/config', config);
  }

  getUsage(date?: string): Observable<AiUsageSummary> {
    return this.api.get<AiUsageSummary>('/ai/admin/usage', date ? { date } : undefined);
  }

  listTemplates(): Observable<PageResponse<AiPromptTemplate>> {
    return this.api.get<PageResponse<AiPromptTemplate>>('/ai/admin/templates');
  }

  saveTemplate(template: AiPromptTemplateRequest): Observable<AiPromptTemplate> {
    return this.api.post<AiPromptTemplate>('/ai/admin/templates', template);
  }
}
