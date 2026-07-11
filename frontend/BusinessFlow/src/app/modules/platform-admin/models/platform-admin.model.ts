// Shared enums
export type CompanyStatus = 'PENDING_VERIFICATION' | 'TRIAL' | 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED';
export const COMPANY_STATUSES: CompanyStatus[] = ['PENDING_VERIFICATION', 'TRIAL', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED'];

export type SubscriptionPlan = 'FREE' | 'STARTER' | 'PRO' | 'ENTERPRISE';
export const SUBSCRIPTION_PLANS: SubscriptionPlan[] = ['FREE', 'STARTER', 'PRO', 'ENTERPRISE'];

export type PlatformRole =
  | 'SUPER_ADMIN' | 'SYSTEM_ADMIN' | 'SUPPORT_AGENT' | 'SUPPORT_MANAGER'
  | 'MARKETING_MANAGER' | 'PLATFORM_ACCOUNTANT' | 'SALES_MANAGER'
  | 'COMPANY_OWNER' | 'CLIENT' | 'EMPLOYEE';
export const PLATFORM_ROLES: PlatformRole[] = [
  'SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_AGENT', 'SUPPORT_MANAGER',
  'MARKETING_MANAGER', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER',
];

// NOTE: PlatformEmploymentType/PlatformEmploymentStatus removed - only used by the
// retired PlatformEmployee payroll model above.

// Companies
export interface Company {
  id: number;
  companyName: string;
  subdomain: string;
  companyEmail?: string;
  companyPhone?: string;
  website?: string;
  address?: string;
  logo?: string;
  primaryColor?: string;
  secondaryColor?: string;
  tagline?: string;
  status: CompanyStatus;
  subscriptionPlan: SubscriptionPlan;
  subscriptionStart?: string;
  subscriptionEnd?: string;
  trialExpired: boolean;
  ownerId?: number;
  ownerName?: string;
  ownerEmail?: string;
  createdAt: string;
}

export interface RegisterCompanyRequest {
  companyName: string;
  subdomain: string;
  ownerFirstName: string;
  ownerLastName: string;
  ownerEmail: string;
  ownerPassword: string;
  companyPhone?: string;
}

// Platform users
export interface PlatformUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  image?: string;
  role: PlatformRole;
  active: boolean;
  emailVerified: boolean;
  languagePreference?: string;
  createdAt: string;
}

export interface PlatformUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: PlatformRole;
  phone?: string;
}

// Custom roles
export interface CustomRole {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  systemRole: boolean;
}

export interface CustomRoleRequest {
  name: string;
  description?: string;
}

// Feature flags
export interface FeatureFlag {
  id: number;
  flagKey: string;
  enabled: boolean;
  description?: string;
  updatedAt?: string;
}

// NOTE: PlatformEmployee/PlatformEmployeeRequest (a richer payroll-style model with
// salary, allowances, deductions, bank details) were removed - confirmed with the user
// that "Platform Employee" is the same concept as PlatformUser below, not a separate
// payroll entity. If platform-staff payroll turns out to be a real, separate need later,
// it would have to be designed and built as its own feature.
