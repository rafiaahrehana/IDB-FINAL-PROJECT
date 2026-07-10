package com.businessos.modules.hrm.attendance.attendance;

import com.businessos.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SecurityUtil securityUtil;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<AttendanceResponse> checkIn(
            @Valid @RequestBody AttendanceCheckInRequest request) {
        return new ResponseEntity<>(attendanceService.checkIn(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceCheckOutRequest request) {
        return ResponseEntity.ok(attendanceService.checkOut(id, request));
    }

    // ── HR / Admin: Manual Entry ──────────────────────────────────────────────

    /**
     * POST /api/v1/company/attendance/manual
     * HR or Admin manually creates an attendance record for any employee / any
     * date.
     */
    @PostMapping("/manual")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<AttendanceResponse> createManual(
            @Valid @RequestBody AttendanceRequest request) {
        return new ResponseEntity<>(attendanceService.createManual(request), HttpStatus.CREATED);
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/company/attendance/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR_MANAGER')")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    /**
     * GET /api/v1/company/attendance/employee/{employeeId}?date=2026-07-03
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR_MANAGER')")
    public ResponseEntity<Page<AttendanceResponse>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(attendanceService.getByEmployee(
                employeeId,
                PageRequest.of(page, size, Sort.by("attendanceDate").descending())));
    }

    /**
     * GET /api/v1/company/attendance/employee/{employeeId}/date?date=2026-07-03
     * Returns a single attendance record for an employee on a specific date.
     */
    @GetMapping("/employee/{employeeId}/date")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR_MANAGER')")
    public ResponseEntity<AttendanceResponse> getByEmployeeAndDate(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByEmployeeAndDate(employeeId, date));
    }

    /**
     * GET
     * /api/v1/company/attendance/date-range?startDate=2026-07-01&endDate=2026-07-31
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Page<AttendanceResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(attendanceService.getByCompanyAndDateRange(
                startDate, endDate, PageRequest.of(page, size, Sort.by("attendanceDate").descending())));
    }

    /**
     * GET /api/v1/company/attendance/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Page<AttendanceResponse>> getByStatus(
            @PathVariable AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(attendanceService.getByStatus(
                status, PageRequest.of(page, size, Sort.by("attendanceDate").descending())));
    }

    @GetMapping("/late")
    @PreAuthorize("hasRole('HR_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getLateAttendances(@RequestParam LocalDate date) {
        return ResponseEntity.ok(attendanceService.getLateAttendances(date));
    }

    @GetMapping("/absent")
    @PreAuthorize("hasRole('HR_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAbsentees(@RequestParam LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAbsentees(date));
    }

    // ── Admin Mutations ───────────────────────────────────────────────────────

    /**
     * PATCH /api/v1/company/attendance/{id}/status
     * Body: { "status": "PRESENT" }
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam AttendanceStatus status) {
        attendanceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH /api/v1/company/attendance/{id}/approve
     * Fixed: was hardcoding "Admin" as approver name. Now derives name from the
     * authenticated principal via SecurityUtil.
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> approveAttendance(@PathVariable Long id) {
        String approverName = securityUtil.getCurrentUser().getFullName();
        attendanceService.approveAttendance(id, approverName);
        return ResponseEntity.ok().build();
    }
}