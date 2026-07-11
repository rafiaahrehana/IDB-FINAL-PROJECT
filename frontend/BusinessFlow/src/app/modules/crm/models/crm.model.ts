export type OpportunityStage =
  | 'PROSPECTING' | 'QUALIFICATION' | 'NEEDS_ANALYSIS'
  | 'PROPOSAL' | 'NEGOTIATION' | 'CLOSED_WON' | 'CLOSED_LOST';

export type CrmActivityType =
  | 'CALL' | 'MEETING' | 'EMAIL' | 'NOTE' | 'TASK'
  | 'FOLLOW_UP' | 'STAGE_CHANGE' | 'STATUS_CHANGE' | 'DOCUMENT';

export const OPEN_STAGES: OpportunityStage[] =
  ['PROSPECTING', 'QUALIFICATION', 'NEEDS_ANALYSIS', 'PROPOSAL', 'NEGOTIATION'];

export interface Lead {
  id: number;
  contactName: string;
  companyName?: string;
  email?: string;
  phone?: string;
  industry?: string;
  jobTitle?: string;
  notes?: string;
  description?: string;
  status: string;
  source: string;
  priority?: string;
  estimatedValue?: number;
  expectedCloseDate?: string;
  converted: boolean;
  assignedToId?: number;
  assignedToName?: string;
  createdAt: string;
}

export interface Client {
  id: number;
  userId?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  image?: string;
  clientCompanyName?: string;
  industry?: string;
  website?: string;
  status: string;
  portalAccessEnabled?: boolean;
  accountManagerId?: number;
  accountManagerName?: string;
  onboardedAt?: string;
  createdAt: string;
}

export interface ClientContact {
  id: number;
  clientId: number;
  fullName: string;
  email?: string;
  phone?: string;
  jobTitle?: string;
  department?: string;
  primaryContact: boolean;
  notes?: string;
  createdAt: string;
}

export interface Opportunity {
  id: number;
  name: string;
  description?: string;
  stage: OpportunityStage;
  source?: string;
  amount?: number;
  probability: number;
  weightedAmount?: number;
  expectedCloseDate?: string;
  actualCloseDate?: string;
  nextStep?: string;
  lostReason?: string;
  clientId: number;
  clientCompanyName?: string;
  contactId?: number;
  contactName?: string;
  ownerId?: number;
  ownerName?: string;
  lastActivityAt?: string;
  stageChangedAt?: string;
  createdAt: string;
}

export interface PipelineStageSummary {
  stage: OpportunityStage;
  dealCount: number;
  totalAmount: number;
  weightedAmount: number;
}

export interface PipelineSummary {
  stages: PipelineStageSummary[];
  openPipelineValue: number;
  weightedForecast: number;
  wonValue: number;
  totalOpenDeals: number;
}

export interface CrmActivity {
  id: number;
  type: CrmActivityType;
  subject: string;
  description?: string;
  activityDate: string;
  scheduledAt?: string;
  completed: boolean;
  systemGenerated: boolean;
  clientId?: number;
  opportunityId?: number;
  opportunityName?: string;
  performedByName?: string;
  createdAt: string;
}
