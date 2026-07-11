export type EmploymentStatus =
  | 'PROBATION' | 'CONFIRMED' | 'ACTIVE' | 'ON_LEAVE'
  | 'SUSPENDED' | 'RESIGNED' | 'TERMINATED' | 'RETIRED';

import { LocationRequest, LocationResponse } from '../../../shared/models/location.model';

export type EmploymentType =
  | 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN' | 'CONSULTANT';

export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';

export const EMPLOYMENT_STATUSES: EmploymentStatus[] =
  ['PROBATION', 'CONFIRMED', 'ACTIVE', 'ON_LEAVE', 'SUSPENDED', 'RESIGNED', 'TERMINATED', 'RETIRED'];

export const EMPLOYMENT_TYPES: EmploymentType[] =
  ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN', 'CONSULTANT'];

export const GENDERS: Gender[] = ['MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY'];

// EmployeeResponse
export interface Employee {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  image?: string;
  employeeNumber?: string;
  officialEmail?: string;
  workPhone?: string;
  profileImageUrl?: string;
  nationalId?: string;
  taxId?: string;
  costCenter?: string;
  officeLocation?: string;
  jobTitle?: string;
  employmentType: EmploymentType;
  employmentStatus: EmploymentStatus;
  gender?: Gender;
  dateOfBirth?: string;
  fatherName?: string;
  motherName?: string;
  location?: LocationResponse;
  hireDate?: string;
  confirmationDate?: string;
  probationEndDate?: string;
  contractEndDate?: string;
  departmentId?: number;
  departmentName?: string;
  designationId?: number;
  designationName?: string;
  reportingManagerId?: number;
  reportingManagerName?: string;
  shiftId?: number;
  shiftName?: string;
  basicSalary?: number;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  bankName?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelation?: string;
  active: boolean;
  createdAt: string;
}

// CreateEmployeeRequest
export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  employmentType: EmploymentType;
  employeeNumber?: string;
  officialEmail?: string;
  workPhone?: string;
  jobTitle?: string;
  designationId?: number;
  employmentStatus?: EmploymentStatus;
  departmentId?: number;
  reportingManagerId?: number;
  shiftId?: number;
  gender?: Gender;
  dateOfBirth?: string;
  fatherName?: string;
  motherName?: string;
  location?: LocationRequest;
  hireDate?: string;
  basicSalary?: number;
  nationalId?: string;
  taxId?: string;
  profileImageUrl?: string;
  costCenter?: string;
  officeLocation?: string;
  confirmationDate?: string;
  probationEndDate?: string;
  contractEndDate?: string;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  bankName?: string;
  bankAccountNumber?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelation?: string;
}

// UpdateEmployeeRequest (all optional on backend)
export interface UpdateEmployeeRequest {
  jobTitle?: string;
  designationId?: number;
  employmentType?: EmploymentType;
  employmentStatus?: EmploymentStatus;
  gender?: Gender;
  dateOfBirth?: string;
  fatherName?: string;
  motherName?: string;
  location?: LocationRequest;
  hireDate?: string;
  confirmationDate?: string;
  probationEndDate?: string;
  contractEndDate?: string;
  departmentId?: number;
  reportingManagerId?: number;
  shiftId?: number;
  basicSalary?: number;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  bankName?: string;
  bankAccountNumber?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelation?: string;
  nationalId?: string;
  taxId?: string;
  costCenter?: string;
  officeLocation?: string;
  workPhone?: string;
  officialEmail?: string;
  profileImageUrl?: string;
}

// DepartmentResponse
export interface Department {
  id: number;
  name: string;
  code?: string;
  description?: string;
  active: boolean;
  budget?: number;
  parentDepartmentId?: number;
  parentDepartmentName?: string;
  headEmployeeId?: number;
  headEmployeeName?: string;
  employeeCount: number;
  createdAt: string;
}

// DepartmentRequest
export interface DepartmentRequest {
  name: string;
  code?: string;
  description?: string;
  headEmployeeId?: number;
  parentDepartmentId?: number;
  budget?: number;
}

// DesignationResponse
export interface Designation {
  id: number;
  name: string;
  code: string;
  level: number;
  description?: string;
  active: boolean;
  createdAt: string;
}

// DesignationRequest
export interface DesignationRequest {
  name: string;
  code: string;
  level: number;
  description?: string;
}

export type PayrollStatus = 'DRAFT' | 'APPROVED' | 'PAID' | 'CANCELLED';

// PayrollResponse
export interface Payroll {
  id: number;
  payMonth: number;
  payYear: number;
  basicSalary: number;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  bonus?: number;
  deductions?: number;
  taxDeduction?: number;
  netSalary: number;
  status: PayrollStatus;
  paymentReference?: string;
  paidAt?: string;
  notes?: string;
  employeeId: number;
  employeeName: string;
  approvedById?: number;
  approvedByName?: string;
  createdAt: string;
}

// CreatePayrollRequest
export interface CreatePayrollRequest {
  employeeId: number;
  payMonth: number;
  payYear: number;
  basicSalary: number;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  bonus?: number;
  deductions?: number;
  taxDeduction?: number;
  notes?: string;
}

// SalaryStructureResponse
export interface SalaryStructure {
  id: number;
  employeeId: number;
  employeeName: string;
  effectiveFrom: string;
  effectiveTo?: string;
  grossSalary: number;
  basicSalary: number;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  foodAllowance?: number;
  specialAllowance?: number;
  providentFund?: number;
  taxDeduction?: number;
  netSalary: number;
  notes?: string;
  approvedById?: number;
  approvedByName?: string;
  createdAt: string;
}

// SalaryStructureRequest
export interface SalaryStructureRequest {
  employeeId: number;
  effectiveFrom: string;
  grossSalary: number;
  basicSalary: number;
  houseRent?: number;
  medicalAllowance?: number;
  transportAllowance?: number;
  foodAllowance?: number;
  specialAllowance?: number;
  providentFund?: number;
  taxDeduction?: number;
  notes?: string;
}

export interface Announcement {
  id: number;
  title: string;
  body: string;
  audience?: string;
  targetDepartmentId?: number;
  targetDepartmentName?: string;
  publishedAt?: string;
  expiresAt?: string;
  published: boolean;
  notifyAll: boolean;
  priority: number;
  attachmentUrl?: string;
  createdById?: number;
  createdByName?: string;
  createdAt: string;
}

export interface AnnouncementRequest {
  title: string;
  body: string;
  audience?: string;
  targetDepartmentId?: number;
  expiresAt?: string;
  notifyAll?: boolean;
  priority?: number;
  attachmentUrl?: string;
}

export interface Holiday {
  id: number;
  name: string;
  holidayDate: string;
  holidayType?: string;
  description?: string;
  departmentId?: number;
  departmentName?: string;
  createdAt: string;
}

export interface HolidayRequest {
  name: string;
  holidayDate?: string;
  holidayType?: string;
  description?: string;
  departmentId?: number;
}

export interface LeavePolicy {
  id: number;
  leaveType: string;
  employmentType: string;
  annualEntitlement: number;
  maxCarryForward: number;
  maxConsecutiveDays?: number;
  requiresApproval: boolean;
  canCarryForward: boolean;
  paid: boolean;
  applicableFromMonths: number;
  active: boolean;
  createdAt: string;
}

export interface LeavePolicyRequest {
  leaveType?: string;
  employmentType?: string;
  annualEntitlement?: number;
  maxCarryForward?: number;
  maxConsecutiveDays?: number;
  requiresApproval?: boolean;
  canCarryForward?: boolean;
  paid?: boolean;
  applicableFromMonths?: number;
}

export interface PerformanceReview {
  id: number;
  reviewPeriodStart: string;
  reviewPeriodEnd: string;
  scoreWorkQuality?: number;
  scoreProductivity?: number;
  scoreCommunication?: number;
  scoreTeamwork?: number;
  scoreInitiative?: number;
  scorePunctuality?: number;
  overallScore?: number;
  strengths?: string;
  areasForImprovement?: string;
  goalsForNextPeriod?: string;
  comments?: string;
  finalised: boolean;
  employeeId: number;
  employeeName?: string;
  reviewedById?: number;
  reviewedByName?: string;
  createdAt: string;
}

export interface PerformanceReviewRequest {
  employeeId?: number;
  reviewPeriodStart?: string;
  reviewPeriodEnd?: string;
  scoreWorkQuality?: number;
  scoreProductivity?: number;
  scoreCommunication?: number;
  scoreTeamwork?: number;
  scoreInitiative?: number;
  scorePunctuality?: number;
  strengths?: string;
  areasForImprovement?: string;
  goalsForNextPeriod?: string;
  comments?: string;
}

export type ShiftType = 'MORNING' | 'AFTERNOON' | 'FULL_DAY' | 'EVENING' | 'NIGHT' | 'FLEXIBLE';
export const SHIFT_TYPES: ShiftType[] = ['MORNING', 'AFTERNOON', 'FULL_DAY', 'EVENING', 'NIGHT', 'FLEXIBLE'];

export interface Shift {
  id: number;
  name: string;
  shiftType: ShiftType;
  startTime: string;
  endTime: string;
  gracePeriodMinutes: number;
  weeklyOffDays?: string;
  flexible: boolean;
  nightShift: boolean;
  active: boolean;
  workingMinutes: number;
  description?: string;
  notes?: string;
  createdAt: string;
}

export interface EmployeeShiftAssignment {
  id: number;
  employeeId: number;
  employeeName?: string;
  shiftId: number;
  shiftName?: string;
  assignmentStartDate?: string;
  assignmentEndDate?: string;
  active: boolean;
  reason?: string;
  assignedBy?: string;
  notes?: string;
}

export interface EmployeeShiftAssignmentRequest {
  employeeId: number;
  shiftId: number;
  assignmentStartDate?: string;
  assignmentEndDate?: string;
  reason?: string;
  assignedBy?: string;
  notes?: string;
}

export interface ShiftRequest {
  name: string;
  shiftType?: ShiftType;
  startTime?: string;
  endTime?: string;
  gracePeriodMinutes?: number;
  weeklyOffDays?: string;
  flexible?: boolean;
  nightShift?: boolean;
  description?: string;
  notes?: string;
}

export type LeaveType = 'ANNUAL' | 'SICK' | 'CASUAL' | 'MATERNITY' | 'PATERNITY' | 'UNPAID' | 'COMPENSATORY';
export const LEAVE_TYPES: LeaveType[] = ['ANNUAL', 'SICK', 'CASUAL', 'MATERNITY', 'PATERNITY', 'UNPAID', 'COMPENSATORY'];

export type HolidayType = 'NATIONAL' | 'RELIGIOUS' | 'OPTIONAL' | 'COMPANY';
export const HOLIDAY_TYPES: HolidayType[] = ['NATIONAL', 'RELIGIOUS', 'OPTIONAL', 'COMPANY'];

export type AnnouncementAudience = 'ALL' | 'EMPLOYEES' | 'MANAGERS' | 'DEPARTMENT' | 'SPECIFIC';
export const ANNOUNCEMENT_AUDIENCES: AnnouncementAudience[] = ['ALL', 'EMPLOYEES', 'MANAGERS', 'DEPARTMENT', 'SPECIFIC'];

export type JobPostingStatus = 'DRAFT' | 'OPEN' | 'CLOSED' | 'ON_HOLD';
export const JOB_POSTING_STATUSES: JobPostingStatus[] = ['DRAFT', 'OPEN', 'CLOSED', 'ON_HOLD'];

export interface JobPosting {
  id: number;
  title: string;
  jobTitle?: string;
  description?: string;
  requirements?: string;
  employmentType?: EmploymentType;
  status: JobPostingStatus;
  vacancies: number;
  salaryMin?: number;
  salaryMax?: number;
  deadline?: string;
  remote: boolean;
  departmentId?: number;
  departmentName?: string;
  createdById?: number;
  createdByName?: string;
  createdAt: string;
}

export interface JobPostingRequest {
  title: string;
  jobTitle?: string;
  description?: string;
  requirements?: string;
  employmentType?: EmploymentType;
  status?: JobPostingStatus;
  vacancies?: number;
  salaryMin?: number;
  salaryMax?: number;
  deadline?: string;
  remote?: boolean;
  departmentId?: number;
}

export type LetterType =
  | 'OFFER' | 'APPOINTMENT' | 'CONFIRMATION' | 'PROMOTION' | 'TRANSFER'
  | 'EXPERIENCE' | 'NOC' | 'SALARY_CERTIFICATE' | 'TERMINATION'
  | 'RESIGNATION_ACCEPTANCE' | 'WARNING' | 'APPRECIATION';
export const LETTER_TYPES: LetterType[] = [
  'OFFER', 'APPOINTMENT', 'CONFIRMATION', 'PROMOTION', 'TRANSFER',
  'EXPERIENCE', 'NOC', 'SALARY_CERTIFICATE', 'TERMINATION',
  'RESIGNATION_ACCEPTANCE', 'WARNING', 'APPRECIATION'
];

export interface OfferLetter {
  id: number;
  letterType: LetterType;
  referenceNumber?: string;
  issueDate: string;
  content: string;
  signedBy?: string;
  fileUrl?: string;
  issued: boolean;
  employeeId: number;
  employeeName?: string;
  createdById?: number;
  createdByName?: string;
  createdAt: string;
}

export interface OfferLetterRequest {
  employeeId?: number;
  letterType?: LetterType;
  referenceNumber?: string;
  issueDate?: string;
  content?: string;
  signedBy?: string;
}

export type LeaveRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
export const LEAVE_REQUEST_STATUSES: LeaveRequestStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

export interface LeaveRequest {
  id: number;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  totalDays: number;
  reason?: string;
  status: LeaveRequestStatus;
  rejectionReason?: string;
  reviewedAt?: string;
  employeeId: number;
  employeeName?: string;
  reviewedById?: number;
  reviewedByName?: string;
  createdAt: string;
}

export interface LeaveRequestPayload {
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  reason?: string;
}

export interface ReviewLeavePayload {
  status: 'APPROVED' | 'REJECTED';
  rejectionReason?: string;
}

export interface LeaveBalance {
  id: number;
  leaveType: LeaveType;
  year: number;
  entitledDays: number;
  usedDays: number;
  pendingDays: number;
  remainingDays: number;
}

export type ApplicationStatus =
  | 'APPLIED' | 'SCREENING' | 'SHORTLISTED' | 'INTERVIEW_SCHEDULED'
  | 'INTERVIEWED' | 'OFFERED' | 'HIRED' | 'REJECTED' | 'WITHDRAWN';
export const APPLICATION_STATUSES: ApplicationStatus[] = [
  'APPLIED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW_SCHEDULED',
  'INTERVIEWED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN'
];

export interface JobApplication {
  id: number;
  applicantName: string;
  applicantEmail: string;
  applicantPhone?: string;
  resumeUrl?: string;
  coverLetter?: string;
  status: ApplicationStatus;
  notes?: string;
  jobPostingId: number;
  jobPostingTitle?: string;
  reviewedById?: number;
  reviewedByName?: string;
  createdAt: string;
}

export interface JobApplicationRequest {
  applicantName: string;
  applicantEmail: string;
  applicantPhone?: string;
  resumeUrl?: string;
  coverLetter?: string;
}

export type HrAssetStatus = 'AVAILABLE' | 'ASSIGNED' | 'UNDER_MAINTENANCE' | 'DISPOSED';
export const HR_ASSET_STATUSES: HrAssetStatus[] = ['AVAILABLE', 'ASSIGNED', 'UNDER_MAINTENANCE', 'DISPOSED'];

export interface HrAsset {
  id: number;
  name: string;
  category?: string;
  serialNumber?: string;
  description?: string;
  purchaseDate?: string;
  purchaseCost?: number;
  status: HrAssetStatus;
  assignedAt?: string;
  returnDate?: string;
  notes?: string;
  assignedToId?: number;
  assignedToName?: string;
  createdAt: string;
  // IT Hardware Specific Fields
  assetTag?: string;
  brand?: string;
  model?: string;
  ipAddress?: string;
  macAddress?: string;
  processorModel?: string;
  ramSize?: string;
  storageSize?: string;
  operatingSystem?: string;
  warrantyExpiry?: string;
}

export interface HrAssetRequest {
  name: string;
  category?: string;
  serialNumber?: string;
  description?: string;
  purchaseDate?: string;
  purchaseCost?: number;
  assignedToId?: number;
  notes?: string;
  assetTag?: string;
  brand?: string;
  model?: string;
  ipAddress?: string;
  macAddress?: string;
  processorModel?: string;
  ramSize?: string;
  storageSize?: string;
  operatingSystem?: string;
  warrantyExpiry?: string;
}

export type HrExpenseStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REIMBURSED';
export const HR_EXPENSE_STATUSES: HrExpenseStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'REIMBURSED'];

export interface HrExpense {
  id: number;
  title: string;
  category?: string;
  amount: number;
  expenseDate: string;
  description?: string;
  receiptUrl?: string;
  status: HrExpenseStatus;
  rejectionReason?: string;
  reimbursedAt?: string;
  submittedById?: number;
  submittedByName?: string;
  approvedById?: number;
  approvedByName?: string;
  createdAt: string;
}

export interface HrExpenseRequest {
  title: string;
  category?: string;
  amount: number;
  expenseDate: string;
  description?: string;
  receiptUrl?: string;
}
