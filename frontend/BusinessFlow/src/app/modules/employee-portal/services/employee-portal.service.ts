import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface EmployeeProfile {
  id: number; userId: number; firstName: string; lastName: string; email: string; phone: string; image: string;
  employeeNumber: string; officialEmail: string; workPhone: string; profileImageUrl: string;
  jobTitle: string; employmentType: string; employmentStatus: string; gender: string;
  dateOfBirth: string; hireDate: string; confirmationDate: string; probationEndDate: string; contractEndDate: string;
  departmentId: number; departmentName: string; designationId: number; designationName: string;
  reportingManagerId: number; reportingManagerName: string; shiftId: number; shiftName: string;
  basicSalary: number; houseRent: number; medicalAllowance: number; transportAllowance: number;
  bankName: string; emergencyContactName: string; emergencyContactPhone: string; emergencyContactRelation: string;
  officeLocation: string; nationalId: string; taxId: string; costCenter: string;
  active: boolean; createdAt: string;
}

export interface LeaveBalance {
  id: number; leaveType: string; year: number; entitledDays: number; usedDays: number; pendingDays: number; remainingDays: number;
}

export interface LeaveRequest {
  id: number; leaveType: string; startDate: string; endDate: string; totalDays: number;
  reason: string; status: string; rejectionReason: string; employeeId: number; employeeName: string; createdAt: string;
}

export interface PayrollRecord {
  id: number; payMonth: number; payYear: number; basicSalary: number; houseRent: number;
  medicalAllowance: number; transportAllowance: number; bonus: number; deductions: number;
  taxDeduction: number; netSalary: number; status: string; paymentReference: string; paidAt: string; createdAt: string;
}

export interface TaskItem {
  id: number; title: string; description: string; status: string; priority: string;
  projectId: number; assigneeId: number; dueDate: string;
}

export interface UserProfile {
  id: number; firstName: string; lastName: string; email: string; phone: string; image: string;
  role: string; languagePreference: string; location: any;
}

@Injectable({ providedIn: 'root' })
export class EmployeePortalService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  getMyProfile(): Observable<EmployeeProfile> {
    return this.http.get<EmployeeProfile>(`${this.api}/employees/me`);
  }

  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.api}/users/profile`);
  }

  updateUserProfile(data: any): Observable<UserProfile> {
    return this.http.patch<UserProfile>(`${this.api}/users/profile`, data);
  }

  getMyLeaves(page = 0, size = 20): Observable<any> {
    return this.http.get<any>(`${this.api}/hr/leaves/my`, { params: { page, size } });
  }

  getMyLeaveBalances(): Observable<LeaveBalance[]> {
    return this.http.get<LeaveBalance[]>(`${this.api}/hr/leaves/balances/my`);
  }

  applyLeave(data: { leaveType: string; startDate: string; endDate: string; reason?: string }): Observable<any> {
    return this.http.post(`${this.api}/hr/leaves`, data);
  }

  cancelLeave(id: number): Observable<any> {
    return this.http.patch(`${this.api}/hr/leaves/${id}/cancel`, {});
  }

  getMyPayroll(page = 0, size = 24): Observable<any> {
    return this.http.get<any>(`${this.api}/hr/payroll/employee/me`, { params: { page, size } });
  }

  getAllTasks(page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.api}/tasks`, { params: { page, size } });
  }

  updateTask(id: number, data: any): Observable<any> {
    return this.http.put(`${this.api}/tasks/${id}`, data);
  }

  getProjects(page = 0, size = 20): Observable<any> {
    return this.http.get<any>(`${this.api}/projects`, { params: { page, size } });
  }

  getMyTimesheets(page = 0, size = 20): Observable<any> {
    return this.http.get<any>(`${this.api}/hr/timesheets/my`, { params: { page, size } });
  }

  logTimesheet(data: any): Observable<any> {
    return this.http.post(`${this.api}/hr/timesheets`, data);
  }

  getMyAttendance(employeeId: number, page = 0, size = 30): Observable<any> {
    return this.http.get<any>(`${this.api}/company/attendance/employee/${employeeId}`, { params: { page, size } });
  }

  checkIn(data: any): Observable<any> {
    return this.http.post(`${this.api}/company/attendance/check-in`, data);
  }

  checkOut(id: number, data: any): Observable<any> {
    return this.http.post(`${this.api}/company/attendance/${id}/check-out`, data);
  }

  getMeetings(page = 0, size = 20): Observable<any> {
    return this.http.get<any>(`${this.api}/meetings`, { params: { page, size } });
  }

  // Admin-level methods (COMPANY_OWNER)
  listEmployees(page = 0, size = 100): Observable<any> {
    return this.http.get<any>(`${this.api}/employees`, { params: { page, size } });
  }

  listDepartments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/departments/active`);
  }

  listPendingLeaves(): Observable<any> {
    return this.http.get<any>(`${this.api}/hr/leaves`, { params: { status: 'PENDING', page: 0, size: 20 } });
  }

  listAnnouncements(): Observable<any> {
    return this.http.get<any>(`${this.api}/announcements`, { params: { page: 0, size: 5 } });
  }

  reviewLeave(leaveId: number, status: 'APPROVED' | 'REJECTED', rejectionReason?: string): Observable<any> {
    const body: any = { status };
    if (rejectionReason) body.rejectionReason = rejectionReason;
    return this.http.patch(`${this.api}/hr/leaves/${leaveId}/review`, body);
  }
}
