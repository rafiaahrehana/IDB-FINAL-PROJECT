package com.businessos.modules.hrm.attendance.reports.controller;

import com.businessos.modules.hrm.attendance.reports.dto.*;
import com.businessos.modules.hrm.attendance.reports.service.AttendanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/company/attendance/reports")
@RequiredArgsConstructor
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<DailyAttendanceReport> getDailyReport(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceReportService.generateDailyReport(date));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<MonthlyAttendanceReport> getMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceReportService.generateMonthlyReport(month, year));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<EmployeeAttendanceSummary> getEmployeeSummary(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceReportService.generateEmployeeSummary(employeeId, startDate, endDate));
    }

    @GetMapping("/department")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<DepartmentAttendanceReport> getDepartmentReport(
            @RequestParam String department,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceReportService.generateDepartmentReport(department, date));
    }

    @GetMapping("/late-absent")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<LateAndAbsentReport> getLateAndAbsentReport(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceReportService.generateLateAbsentReport(date));
    }
}
