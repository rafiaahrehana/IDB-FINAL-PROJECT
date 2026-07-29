package com.businessos.modules.hrm.attendance.attendance;

import com.businessos.modules.hrm.attendance.biometric.device.BiometricDevice;
import com.businessos.modules.hrm.attendance.biometric.device.BiometricDeviceRepository;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignment;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignmentRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.hrm.leave.holiday.HolidayRepository;
import com.businessos.modules.hrm.leave.leaverequest.LeaveRequest;
import com.businessos.modules.hrm.leave.leaverequest.LeaveRequestRepository;
import com.businessos.enums.LeaveRequestStatus;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.auth.user.User;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ForbiddenException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final BiometricDeviceRepository deviceRepository;
    private final EmployeeShiftAssignmentRepository shiftAssignmentRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    private void requireViewOrOwn(Long employeeId) {
        if (authorizationService.hasPermission(PermissionCode.ATTENDANCE_VIEW)) {
            return;
        }
        User currentUser = securityUtil.getCurrentUser();
        Employee currentEmployee = currentUser != null
                ? employeeRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;
        if (currentEmployee == null || employeeId == null || !currentEmployee.getId().equals(employeeId)) {
            throw new ForbiddenException("Access denied: you can only access your own attendance records");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getMyTodayAttendance() {
        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        Employee employee = employeeRepository.findByUserId(currentUser.getId()).orElse(null);
        if (employee == null) {
            return null;
        }
        List<Attendance> attendances = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), LocalDate.now());
        if (attendances.isEmpty()) {
            return null;
        }
        return AttendanceMapper.toResponse(attendances.get(attendances.size() - 1));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMyRecords(Pageable pageable) {
        User currentUser = securityUtil.getCurrentUser();
        Employee employee = currentUser != null
            ? employeeRepository.findByUserId(currentUser.getId()).orElse(null)
            : null;
        if (employee == null) {
            return Page.empty(pageable);
        }
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndEmployeeId(companyId, employee.getId(), pageable)
            .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MyAttendanceMonthlySummaryResponse getMyMonthlySummary(int year, int month) {
        MyAttendanceMonthlySummaryResponse summary = new MyAttendanceMonthlySummaryResponse();
        summary.setYear(year);
        summary.setMonth(month);

        User currentUser = securityUtil.getCurrentUser();
        Employee employee = currentUser != null
            ? employeeRepository.findByUserId(currentUser.getId()).orElse(null)
            : null;
        if (employee == null) {
            summary.setWorkedHours(BigDecimal.ZERO);
            return summary;
        }
        Long companyId = securityUtil.getCurrentCompanyId();

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate today = LocalDate.now();
        LocalDate elapsedEnd = monthEnd.isAfter(today) ? today : monthEnd;

        List<Attendance> records = attendanceRepository.findByEmployeeAndDateRange(employee.getId(), monthStart, monthEnd);
        BigDecimal workedHours = BigDecimal.ZERO;
        int present = 0, absent = 0, halfDay = 0;
        for (Attendance a : records) {
            if (a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE
                    || a.getStatus() == AttendanceStatus.WORK_FROM_HOME) {
                present++;
            } else if (a.getStatus() == AttendanceStatus.ABSENT) {
                absent++;
            } else if (a.getStatus() == AttendanceStatus.HALF_DAY) {
                halfDay++;
            }
            if (a.getTotalWorkingHours() != null) {
                workedHours = workedHours.add(a.getTotalWorkingHours());
            }
        }
        summary.setPresentDays(present);
        summary.setAbsentDays(absent);
        summary.setHalfDays(halfDay);
        summary.setWorkedHours(workedHours);

        summary.setHolidayDays(holidayRepository.findByCompanyAndDateRange(companyId, monthStart, monthEnd).size());

        List<LeaveRequest> approvedLeave = leaveRequestRepository.findApprovedOverlapping(
            employee.getId(), LeaveRequestStatus.APPROVED, monthStart, monthEnd);
        int onLeaveDays = 0;
        for (LeaveRequest lr : approvedLeave) {
            LocalDate start = lr.getStartDate().isBefore(monthStart) ? monthStart : lr.getStartDate();
            LocalDate end = lr.getEndDate().isAfter(monthEnd) ? monthEnd : lr.getEndDate();
            if (!end.isBefore(start)) {
                onLeaveDays += (int) (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1);
            }
        }
        summary.setOnLeaveDays(onLeaveDays);

        String weeklyOffDays = shiftAssignmentRepository
            .findByCompanyIdAndEmployeeIdAndActive(companyId, employee.getId())
            .map(a -> a.getShift().getWeeklyOffDays())
            .orElse("FRI,SAT");
        List<String> offDayAbbreviations = weeklyOffDays == null || weeklyOffDays.isBlank()
            ? List.of() : Arrays.asList(weeklyOffDays.split(","));
        int weekOffDays = 0;
        for (LocalDate d = monthStart; !d.isAfter(elapsedEnd); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (offDayAbbreviations.contains(dow.name().substring(0, 3))) {
                weekOffDays++;
            }
        }
        summary.setWeekOffDays(weekOffDays);

        return summary;
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckInRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        Long empId = request.getEmployeeId();
        if (empId == null) {
            User currentUser = securityUtil.getCurrentUser();
            if (currentUser != null) {
                Employee emp = employeeRepository.findByUserId(currentUser.getId()).orElse(null);
                if (emp != null) {
                    empId = emp.getId();
                }
            }
        }
        if (empId == null) {
            throw new ResourceNotFoundException("Employee not found");
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (companyId == null && employee.getCompany() != null) {
            companyId = employee.getCompany().getId();
        }

        BiometricDevice device = null;
        if (request.getDeviceId() != null) {
            device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        }

        // Check if attendance already exists for today
        List<Attendance> existingList = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(empId, LocalDate.now());

        Attendance attendance;
        if (!existingList.isEmpty()) {
            // Update existing
            attendance = existingList.get(0);
        } else {
            EmployeeShiftAssignment shift = shiftAssignmentRepository
                    .findByCompanyIdAndEmployeeIdAndActive(companyId, empId)
                    .orElse(null);

            attendance = Attendance.builder()
                    .companyId(companyId)
                    .employee(employee)
                    .attendanceDate(LocalDate.now())
                    .shiftType(shift != null ? shift.getShift().getShiftType() : null)
                    .status(AttendanceStatus.PRESENT)
                    .build();
        }

        java.time.LocalTime checkInTime = request.getCheckInTime() != null ? request.getCheckInTime() : java.time.LocalTime.now();
        AttendanceMethod method = request.getMethod() != null ? request.getMethod() : AttendanceMethod.MANUAL;

        // Record check-in
        attendance.checkIn(checkInTime, method, device);
        attendance.setVerified(request.isVerified());
        attendance.setVerificationScore(request.getVerificationScore());

        applyLateDetection(attendance, companyId, empId, checkInTime);

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    /**
     * Compares check-in time against the employee's assigned shift (start time +
     * grace period) and marks the attendance LATE if it exceeds it. No-op if the
     * employee has no active shift assignment - callers keep whatever late flag
     * they already set (e.g. a manual HR override).
     */
    private void applyLateDetection(Attendance attendance, Long companyId, Long employeeId,
                                     java.time.LocalTime checkInTime) {
        if (checkInTime == null) return;

        EmployeeShiftAssignment shiftAssignment = shiftAssignmentRepository
                .findByCompanyIdAndEmployeeIdAndActive(companyId, employeeId)
                .orElse(null);
        if (shiftAssignment == null) return;

        long lateMinutes = java.time.temporal.ChronoUnit.MINUTES
                .between(shiftAssignment.getShift().getStartTime(), checkInTime);

        if (lateMinutes > shiftAssignment.getShift().getGracePeriodMinutes()) {
            attendance.setLate(true);
            attendance.setLateMinutes(lateMinutes);
            attendance.setStatus(AttendanceStatus.LATE);
        } else {
            attendance.setLate(false);
            attendance.setLateMinutes(0);
        }
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long attendanceId, AttendanceCheckOutRequest request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));

        BiometricDevice device = null;
        if (request.getDeviceId() != null) {
            device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        }

        java.time.LocalTime checkOutTime = request.getCheckOutTime() != null ? request.getCheckOutTime() : java.time.LocalTime.now();
        AttendanceMethod method = request.getMethod() != null ? request.getMethod() : AttendanceMethod.MANUAL;

        attendance.checkOut(checkOutTime, method, device);

        // Calculate total hours
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            BigDecimal totalHours = attendance.calculateTotalHours();
            attendance.setTotalWorkingHours(totalHours);
        }

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        requireViewOrOwn(attendance.getEmployee() != null ? attendance.getEmployee().getId() : null);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getByEmployeeAndDate(Long employeeId, LocalDate date) {
        requireViewOrOwn(employeeId);
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);
        if (attendances.isEmpty()) {
            throw new ResourceNotFoundException("Attendance not found");
        }
        return AttendanceMapper.toResponse(attendances.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByEmployee(Long employeeId, Pageable pageable) {
        requireViewOrOwn(employeeId);
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndEmployeeId(companyId, employeeId, pageable)
                .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByCompanyAndDateRange(LocalDate start, LocalDate end, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndAttendanceDateBetween(companyId, start, end, pageable)
                .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByStatus(AttendanceStatus status, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> listAll(AttendanceStatus status, LocalDate date, LocalDate startDate, LocalDate endDate, String search, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        boolean isHrOrAdmin = authorizationService.hasPermission(PermissionCode.ATTENDANCE_VIEW);

        if (!isHrOrAdmin) {
            User currentUser = securityUtil.getCurrentUser();
            Employee currentEmployee = currentUser != null
                    ? employeeRepository.findByUserId(currentUser.getId()).orElse(null)
                    : null;
            if (currentEmployee == null) {
                return Page.empty(pageable);
            }
            return attendanceRepository.findByCompanyIdAndEmployeeId(companyId, currentEmployee.getId(), pageable)
                    .map(AttendanceMapper::toResponse);
        }

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatus = status != null;
        boolean hasDate = date != null;
        boolean hasStart = startDate != null;
        boolean hasEnd = endDate != null;

        LocalDate start = hasDate ? date : (hasStart ? startDate : null);
        LocalDate end = hasDate ? date : (hasEnd ? endDate : null);

        // 1. No search keyword active
        if (!hasSearch) {
            // No filters -> standard default query
            if (!hasStatus && start == null && end == null) {
                return attendanceRepository.findByCompanyId(companyId, pageable)
                        .map(AttendanceMapper::toResponse);
            }
            // Status only -> standard status query
            if (hasStatus && start == null && end == null) {
                return attendanceRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                        .map(AttendanceMapper::toResponse);
            }
            // Date / date-range only -> standard date query
            if (!hasStatus && (start != null || end != null)) {
                LocalDate s = start != null ? start : LocalDate.of(1970, 1, 1);
                LocalDate e = end != null ? end : LocalDate.of(2099, 12, 31);
                return attendanceRepository.findByCompanyIdAndAttendanceDateBetween(companyId, s, e, pageable)
                        .map(AttendanceMapper::toResponse);
            }
            // Status + Date / date-range
            if (hasStatus && (start != null || end != null)) {
                LocalDate s = start != null ? start : LocalDate.of(1970, 1, 1);
                LocalDate e = end != null ? end : LocalDate.of(2099, 12, 31);
                return attendanceRepository.searchByStatusAndDateRange(companyId, status, s, e, pageable)
                        .map(AttendanceMapper::toResponse);
            }
        }

        // 2. Search keyword active
        String searchKeyword = search.trim();
        LocalDate s = start != null ? start : LocalDate.of(1970, 1, 1);
        LocalDate e = end != null ? end : LocalDate.of(2099, 12, 31);
        if (hasStatus) {
            return attendanceRepository.searchAttendanceRecordsWithStatusAndDate(companyId, status, s, e, searchKeyword, pageable)
                    .map(AttendanceMapper::toResponse);
        } else {
            return attendanceRepository.searchAttendanceRecordsWithoutStatusAndDate(companyId, s, e, searchKeyword, pageable)
                    .map(AttendanceMapper::toResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getLateAttendances(LocalDate date) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findLateAttendances(companyId, date)
                .stream()
                .map(AttendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAbsentees(LocalDate date) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndStatusAndAttendanceDateBetween(
                companyId, AttendanceStatus.ABSENT, date, date)
                .stream()
                .map(AttendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countPresent(Long companyId, LocalDate date) {
        return attendanceRepository.countByCompanyIdAndStatusAndDate(companyId, AttendanceStatus.PRESENT, date);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLate(Long companyId, LocalDate date) {
        return attendanceRepository.countByCompanyIdAndStatusAndDate(companyId, AttendanceStatus.LATE, date);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAbsent(Long companyId, LocalDate date) {
        return attendanceRepository.countByCompanyIdAndStatusAndDate(companyId, AttendanceStatus.ABSENT, date);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, AttendanceStatus status) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_APPROVE);
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void approveAttendance(Long id, String approverName) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_APPROVE);
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.setApproved(true);
        attendance.setApprovedBy(approverName);
        attendance.setApprovedDateTime(LocalDateTime.now());
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse delete(Long id) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_APPROVE);
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.softDelete();
        attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse createManual(AttendanceRequest request) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_MARK);
        Long companyId = securityUtil.getCurrentCompanyId();
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<Attendance> existingList = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), request.getAttendanceDate());

        Attendance attendance;
        if (!existingList.isEmpty()) {
            attendance = existingList.get(0);
            if (request.getStatus() != null) attendance.setStatus(request.getStatus());
            if (request.getCheckInTime() != null) attendance.setCheckInTime(request.getCheckInTime());
            if (request.getCheckOutTime() != null) attendance.setCheckOutTime(request.getCheckOutTime());
            if (request.getShiftType() != null) attendance.setShiftType(request.getShiftType());
            if (request.getOvertimeHours() != null) attendance.setOvertimeHours(request.getOvertimeHours());
            if (request.getAdminNotes() != null) attendance.setAdminNotes(request.getAdminNotes());
        } else {
            attendance = Attendance.builder()
                    .companyId(companyId)
                    .employee(employee)
                    .attendanceDate(request.getAttendanceDate())
                    .status(request.getStatus() != null ? request.getStatus() : AttendanceStatus.PRESENT)
                    .checkInTime(request.getCheckInTime())
                    .checkOutTime(request.getCheckOutTime())
                    .shiftType(request.getShiftType())
                    .overtimeHours(request.getOvertimeHours())
                    .isLate(request.isLate())
                    .lateMinutes(request.getLateMinutes())
                    .isVerified(true)
                    .build();
        }

        // Auto-detect lateness from the employee's assigned shift when a check-in time is given
        applyLateDetection(attendance, companyId, request.getEmployeeId(), attendance.getCheckInTime());
        if (attendance.isLate() && attendance.getStatus() != AttendanceStatus.LATE) {
            attendance.setStatus(AttendanceStatus.LATE);
        }

        // Calculate total hours
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            BigDecimal totalHours = attendance.calculateTotalHours();
            attendance.setTotalWorkingHours(totalHours);
        }

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }
}