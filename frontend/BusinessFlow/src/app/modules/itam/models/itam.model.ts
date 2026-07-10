// NOTE: Hardware now reuses hrm/models/hrm.model.ts's HrAsset - there is no dedicated
// hardware controller/entity, only the shared hrm/asset backend. This interface is dead.

export interface OffboardingChecklist {
  id: number;
  employeeId: number;
  employeeName?: string;
  offboardingDate?: string;
  targetCompletionDate?: string;
  hardwareCollected: boolean;
  hardwareCollectedDate?: string;
  licensesRevoked: boolean;
  licensesRevokedDate?: string;
  accessRevoked: boolean;
  accessRevokedDate?: string;
  dataHandedOver: boolean;
  dataHandoverDate?: string;
  exitInterviewCompleted: boolean;
  exitInterviewDate?: string;
  completed: boolean;
  completionDate?: string;
  completionPercentage: number;
  overallNotes?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface OffboardingChecklistRequest {
  employeeId: number;
  notes?: string;
}

export interface AssetImportRowError {
  row: number;
  reason: string;
}

export interface AssetImportResult {
  totalRows: number;
  succeeded: number;
  failed: number;
  errors: AssetImportRowError[];
}

export interface SoftwareLicense {
  id: number;
  companyId: number;
  licenseKey?: string;
  softwareName: string;
  publisher?: string;
  version?: string;
  licenseType: string;
  licenseStatus: string;
  totalSeatsLicensed: number;
  seatsUsed: number;
  seatsAvailable: number;
  licensePurchaseDate?: string;
  licenseCost?: number;
  licenseExpiryDate?: string;
  expiringSoon: boolean;
  expired: boolean;
  daysUntilExpiry: number;
  renewalType?: string;
  nextRenewalDate?: string;
  renewalCost?: number;
  vendor?: string;
  accountEmail?: string;
  licenseUrl?: string;
  installationLocation?: string;
  estimatedUserCount?: number;
  notes?: string;
  active: boolean;
  autoRenew: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface AssetAssignment {
  id: number;
  hardwareAssetId: number;
  assetName?: string;
  assetTag?: string;
  employeeId: number;
  employeeName?: string;
  assignedDate: string;
  returnDate?: string;
  returnedAt?: string;
  notes?: string;
}
