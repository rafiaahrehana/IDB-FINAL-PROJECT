package com.businessos.modules.hrm.attendance.timesheet;

import com.businessos.auth.user.User;
import com.businessos.modules.hrm.employee.Employee;
import java.time.LocalDateTime;

public class TimesheetMapper {
    public static TimesheetResponse toTimesheetResponse(Timesheet t) {
        Employee emp = t.getEmployee();
        User empUser = emp != null ? emp.getUser() : null;
        User approverUser = t.getApprovedBy();
        
        TimesheetResponse r = new TimesheetResponse();
        r.setId(t.getId());
        r.setWorkDate(t.getWorkDate());
        r.setStartTime(t.getStartTime() != null ? LocalDateTime.of(t.getWorkDate(), t.getStartTime()) : null);
        r.setEndTime(t.getEndTime() != null ? LocalDateTime.of(t.getWorkDate(), t.getEndTime()) : null);
        r.setHoursWorked(t.getHoursWorked());
        r.setBillableHours(t.getBillableHours());
        r.setDescription(t.getWorkSummary());
        r.setApproved(t.isApproved());
        
        r.setEmployeeId(emp != null ? emp.getId() : null);
        r.setEmployeeName(empUser != null ? empUser.getFullName() : null);
        r.setApprovedById(approverUser != null ? approverUser.getId() : null);
        r.setApprovedByName(approverUser != null ? approverUser.getFullName() : null);
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}
