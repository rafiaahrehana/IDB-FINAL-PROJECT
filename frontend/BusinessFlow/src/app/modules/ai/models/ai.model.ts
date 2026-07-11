export type AiProviderType = 'GEMINI' | 'CLAUDE' | 'OPENAI' | 'MOCK';
export type AiModel =
  | 'GEMINI_2_5_FLASH'
  | 'GEMINI_2_5_PRO'
  | 'GPT_4O'
  | 'GPT_4O_MINI'
  | 'CLAUDE_SONNET'
  | 'CLAUDE_OPUS';
export type AiFeature =
  | 'EMPLOYMENT_LETTER'
  | 'LEAVE_POLICY'
  | 'PERFORMANCE_REVIEW'
  | 'CRM_LEAD_SUMMARY'
  | 'CRM_ACTIVITY_SUMMARY'
  | 'INVOICE_SUMMARY'
  | 'SERVICE_REQUEST_SUMMARY'
  | 'ANNOUNCEMENT_DRAFT'
  | 'WORKFLOW_SUGGESTION'
  | 'SEARCH_ANSWER'
  | 'BUSINESS_INSIGHTS'
  | 'GENERAL';

export interface AiProviderConfig {
  id?: number;
  provider: AiProviderType;
  model: AiModel;
  temperature: number;
  maxTokens: number;
  apiKey?: string;
}

export interface AiProviderConfigRequest {
  aiProviderType: AiProviderType;
  model: AiModel;
  temperature: number;
  maxTokens: number;
  apiKey?: string;
}

export interface AiUsageSummary {
  totalRequests: number;
  totalTokens?: number;
  totalInputTokens: number;
  totalOutputTokens: number;
  totalCostUsd: number;
  avgResponseTimeMs?: number;
  requestsByFeature?: { [key: string]: number };
  tokensByFeature?: { [key: string]: number };
}

export interface AiPromptTemplate {
  id: number;
  feature: AiFeature;
  name: string;
  template: string;
  version: number;
  changeNotes?: string;
}

export interface AiPromptTemplateRequest {
  feature: AiFeature;
  name: string;
  template: string;
  changeNotes?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
