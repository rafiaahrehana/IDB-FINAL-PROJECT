export type PlatformRole =
  | 'SUPER_ADMIN'
  | 'SYSTEM_ADMIN'
  | 'SUPPORT_AGENT'
  | 'SUPPORT_MANAGER'
  | 'MARKETING_MANAGER'
  | 'PLATFORM_ACCOUNTANT'
  | 'SALES_MANAGER';

export type TenantRole = 'COMPANY_OWNER' | 'CLIENT' | 'EMPLOYEE';

export type Role = PlatformRole | TenantRole;

export type PermissionCode =
  | 'COMPANY_VIEW' | 'COMPANY_UPDATE' | 'COMPANY_SETTINGS' | 'COMPANY_BRANDING'
  | 'USER_VIEW' | 'USER_CREATE' | 'USER_UPDATE' | 'USER_DELETE'
  | 'EMPLOYEE_VIEW' | 'EMPLOYEE_CREATE' | 'EMPLOYEE_UPDATE' | 'EMPLOYEE_DELETE'
  | 'DEPARTMENT_VIEW' | 'DEPARTMENT_CREATE' | 'DEPARTMENT_UPDATE' | 'DEPARTMENT_DELETE'
  | 'DESIGNATION_VIEW' | 'DESIGNATION_CREATE' | 'DESIGNATION_UPDATE' | 'DESIGNATION_DELETE'
  | 'LEAVE_VIEW' | 'LEAVE_CREATE' | 'LEAVE_UPDATE' | 'LEAVE_CANCEL' | 'LEAVE_APPROVE' | 'LEAVE_REJECT'
  | 'EXPENSE_VIEW' | 'EXPENSE_CREATE' | 'EXPENSE_UPDATE' | 'EXPENSE_DELETE' | 'EXPENSE_APPROVE' | 'EXPENSE_REJECT'
  | 'ATTENDANCE_VIEW' | 'ATTENDANCE_MARK' | 'ATTENDANCE_UPDATE'
  | 'PAYROLL_VIEW' | 'PAYROLL_PROCESS' | 'PAYROLL_APPROVE'
  | 'SERVICE_REQUEST_VIEW' | 'SERVICE_REQUEST_CREATE' | 'SERVICE_REQUEST_ASSIGN' | 'SERVICE_REQUEST_APPROVE' | 'SERVICE_REQUEST_CLOSE'
  | 'CLIENT_VIEW' | 'CLIENT_CREATE' | 'CLIENT_UPDATE' | 'CLIENT_DELETE'
  | 'AI_CHAT' | 'AI_ADMIN';

export const PLATFORM_ROLES: PlatformRole[] = [
  'SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_AGENT', 'SUPPORT_MANAGER',
  'MARKETING_MANAGER', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER',
];

export const TENANT_ROLES: TenantRole[] = ['COMPANY_OWNER', 'CLIENT', 'EMPLOYEE'];

export const PLATFORM_ADMIN_ROLES: PlatformRole[] = ['SUPER_ADMIN', 'SYSTEM_ADMIN'];

export const ALL_PERMISSIONS: Record<PermissionCode, string> = {
  COMPANY_VIEW: 'View Company',
  COMPANY_UPDATE: 'Update Company',
  COMPANY_SETTINGS: 'Manage Company Settings',
  COMPANY_BRANDING: 'Manage Branding',
  USER_VIEW: 'View Users',
  USER_CREATE: 'Create Users',
  USER_UPDATE: 'Update Users',
  USER_DELETE: 'Delete Users',
  EMPLOYEE_VIEW: 'View Employees',
  EMPLOYEE_CREATE: 'Create Employees',
  EMPLOYEE_UPDATE: 'Update Employees',
  EMPLOYEE_DELETE: 'Delete Employees',
  DEPARTMENT_VIEW: 'View Departments',
  DEPARTMENT_CREATE: 'Create Departments',
  DEPARTMENT_UPDATE: 'Update Departments',
  DEPARTMENT_DELETE: 'Delete Departments',
  DESIGNATION_VIEW: 'View Designations',
  DESIGNATION_CREATE: 'Create Designations',
  DESIGNATION_UPDATE: 'Update Designations',
  DESIGNATION_DELETE: 'Delete Designations',
  LEAVE_VIEW: 'View Leaves',
  LEAVE_CREATE: 'Apply Leave',
  LEAVE_UPDATE: 'Update Leave',
  LEAVE_CANCEL: 'Cancel Leave',
  LEAVE_APPROVE: 'Approve Leaves',
  LEAVE_REJECT: 'Reject Leaves',
  EXPENSE_VIEW: 'View Expenses',
  EXPENSE_CREATE: 'Create Expenses',
  EXPENSE_UPDATE: 'Update Expenses',
  EXPENSE_DELETE: 'Delete Expenses',
  EXPENSE_APPROVE: 'Approve Expenses',
  EXPENSE_REJECT: 'Reject Expenses',
  ATTENDANCE_VIEW: 'View Attendance',
  ATTENDANCE_MARK: 'Mark Attendance',
  ATTENDANCE_UPDATE: 'Update Attendance',
  PAYROLL_VIEW: 'View Payroll',
  PAYROLL_PROCESS: 'Process Payroll',
  PAYROLL_APPROVE: 'Approve Payroll',
  SERVICE_REQUEST_VIEW: 'View Service Requests',
  SERVICE_REQUEST_CREATE: 'Create Service Requests',
  SERVICE_REQUEST_ASSIGN: 'Assign Service Requests',
  SERVICE_REQUEST_APPROVE: 'Approve Service Requests',
  SERVICE_REQUEST_CLOSE: 'Close Service Requests',
  CLIENT_VIEW: 'View Clients',
  CLIENT_CREATE: 'Create Clients',
  CLIENT_UPDATE: 'Update Clients',
  CLIENT_DELETE: 'Delete Clients',
  AI_CHAT: 'Use AI Chat',
  AI_ADMIN: 'Administer AI',
};

export interface NavGroup {
  label: string;
  icon: string;
  items: NavItem[];
  roles?: Role[];
  permissions?: PermissionCode[];
}

export interface NavItem {
  label: string;
  route: string;
  icon: string;
  roles?: Role[];
  permissions?: PermissionCode[];
}
