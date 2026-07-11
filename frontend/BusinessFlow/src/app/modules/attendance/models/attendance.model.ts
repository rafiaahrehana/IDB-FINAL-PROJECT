export interface AttendanceRecord {
  id: number;
  companyId: number;
  employeeId: number;
  employeeName: string;
  employeeNumber?: string;
  attendanceDate: string;
  checkInTime?: string;
  checkOutTime?: string;
  checkInDateTime?: string;
  checkOutDateTime?: string;
  status: string;
  isLate: boolean;
  lateMinutes: number;
  isOvertime: boolean;
  overtimeHours?: number;
  totalWorkingHours?: number;
  approved: boolean;
  approvedBy?: string;
  notes?: string;
  createdAt: string;
}

export interface AttendanceLeave {
  id: number;
  companyId: number;
  employeeId: number;
  employeeName: string;
  leaveDate: string;
  leaveType: string;
  leaveReason?: string;
  halfDay: boolean;
  approved: boolean;
  approvedBy?: string;
  rejectionReason?: string;
  notes?: string;
  createdAt: string;
}

export interface Timesheet {
  id: number;
  workDate: string;
  startTime?: string;
  endTime?: string;
  hoursWorked: number;
  billableHours: number;
  projectName?: string;
  taskDescription?: string;
  description?: string;
  approved: boolean;
  approvedAt?: string;
  employeeId: number;
  employeeName?: string;
  approvedById?: number;
  approvedByName?: string;
  taskId?: number;
  taskTitle?: string;
  createdAt: string;
}

export interface TimesheetRequest {
  workDate: string;
  startTime?: string;
  endTime?: string;
  hoursWorked: number;
  billableHours?: number;
  projectName?: string;
  taskDescription?: string;
  description?: string;
  taskId?: number;
}

export type BiometricDeviceType = 'FINGERPRINT_TERMINAL' | 'FACIAL_RECOGNITION' | 'RFID_READER' | 'IRIS_SCANNER' | 'HYBRID' | 'GPS_TRACKING' | 'NFC_READER' | 'QR_SCANNER';
export const BIOMETRIC_DEVICE_TYPES: BiometricDeviceType[] = ['FINGERPRINT_TERMINAL', 'FACIAL_RECOGNITION', 'RFID_READER', 'IRIS_SCANNER', 'HYBRID', 'GPS_TRACKING', 'NFC_READER', 'QR_SCANNER'];

export type BiometricDeviceStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'OFFLINE';
export const BIOMETRIC_DEVICE_STATUSES: BiometricDeviceStatus[] = ['ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE'];

export interface BiometricDevice {
  id: number;
  deviceName: string;
  deviceType: BiometricDeviceType;
  deviceId: string;
  ipAddress?: string;
  portNumber: number;
  location?: string;
  department?: string;
  status: BiometricDeviceStatus;
  matchThreshold: number;
  enabledForCheckIn: boolean;
  enabledForCheckOut: boolean;
  lastSyncTime?: string;
  lastHealthCheckTime?: string;
  isOnline: boolean;
  manufacturer?: string;
  model?: string;
  firmwareVersion?: string;
  totalEnrollments: number;
  maxEnrollments: number;
  notes?: string;
}

export interface BiometricDeviceRequest {
  deviceName: string;
  deviceType: BiometricDeviceType;
  deviceId: string;
  ipAddress?: string;
  portNumber?: number;
  location?: string;
  department?: string;
  matchThreshold?: number;
  enabledForCheckIn?: boolean;
  enabledForCheckOut?: boolean;
  notes?: string;
}

export interface BiometricEnrollment {
  id: number;
  employeeId: number;
  employeeName?: string;
  deviceId: number;
  deviceName?: string;
  biometricType: string;
  biometricTemplate: string;
  templateFormat?: string;
  enrollmentDate?: string;
  enrolledBy?: string;
  enrollmentAttempts: number;
  enrollmentQualityScore: number;
  enrolled: boolean;
  active: boolean;
  lastVerifiedTime?: string;
  successfulMatches: number;
  failedMatches: number;
  notes?: string;
  securityNotes?: string;
}

export interface BiometricEnrollmentRequest {
  employeeId: number;
  deviceId: number;
  biometricType: string;
  biometricTemplate: string;
  templateFormat?: string;
  qualityScore?: number;
}
