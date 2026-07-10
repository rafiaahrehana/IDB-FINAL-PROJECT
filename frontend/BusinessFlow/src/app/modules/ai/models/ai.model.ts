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
  id: number;
  provider: AiProviderType;
  model: AiModel;
  temperature: number;
  maxTokens: number;
  active: boolean;
  createdAt?: string;
}

export interface AiProviderConfigRequest {
  aiProviderType: AiProviderType;
  model: AiModel;
  apiKey?: string;
  temperature?: number;
  maxTokens?: number;
}

export interface AiUsageSummary {
  date?: string;
  totalRequests: number;
  totalTokens: number;
  avgResponseTimeMs: number;
  requestsByFeature?: Record<string, number>;
  tokensByFeature?: Record<string, number>;
}

export interface AiPromptTemplate {
  id: number;
  feature: AiFeature;
  name: string;
  template: string;
  version: number;
  active: boolean;
  changeNotes?: string;
}

export interface AiPromptTemplateRequest {
  feature: AiFeature;
  name: string;
  template: string;
  changeNotes?: string;
}
