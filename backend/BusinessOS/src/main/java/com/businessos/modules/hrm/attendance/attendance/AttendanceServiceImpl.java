package com.businessos.modules.hrm.attendance.attendance;

import com.businessos.modules.hrm.attendance.biometric.device.BiometricDevice;
import com.businessos.modules.hrm.attendance.biometric.device.BiometricDeviceRepository;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignment;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignmentRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckInRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        BiometricDevice device = null;
        if (request.getDeviceId() != null) {
            device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        }

        // Check if attendance already exists for today
        Optional<Attendance> existingAttendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), LocalDate.now());

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            // Update existing
            attendance = existingAttendance.get();
        } else {
            EmployeeShiftAssignment shift = shiftAssignmentRepository
                    .findByCompanyIdAndEmployeeIdAndActive(companyId, request.getEmployeeId())
                    .orElse(null);

            attendance = Attendance.builder()
                    .companyId(companyId)
                    .employee(employee)
                    .attendanceDate(LocalDate.now())
                    .shiftType(shift != null ? shift.getShift().getShiftType() : null)
                    .status(AttendanceStatus.PRESENT)
                    .build();
        }

        // Record check-in
        attendance.checkIn(request.getCheckInTime(), request.getMethod(), device);
        attendance.setVerified(request.isVerified());
        attendance.setVerificationScore(request.getVerificationScore());

        // Check if late
        EmployeeShiftAssignment shiftAssignment = shiftAssignmentRepository
                .findByCompanyIdAndEmployeeIdAndActive(companyId, request.getEmployeeId())
                .orElse(null);

        if (shiftAssignment != null) {
            long lateMinutes = java.time.temporal.ChronoUnit.MINUTES
                    .between(shiftAssignment.getShift().getStartTime(), request.getCheckInTime());

            if (lateMinutes > shiftAssignment.getShift().getGracePeriodMinutes()) {
                attendance.setLate(true);
                attendance.setLateMinutes(lateMinutes);
                attendance.setStatus(AttendanceStatus.LATE);
            }
        }

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
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

        attendance.checkOut(request.getCheckOutTime(), request.getMethod(), device);

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
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getByEmployeeAndDate(Long employeeId, LocalDate date) {
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByEmployee(Long employeeId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndEmployeeId(companyId, employeeId, pageable)
                .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByCompanyAndDateRange(LocalDate start, LocalDate end, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndAttendanceDateBetween(companyId, start, end, pageable)
                .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByStatus(AttendanceStatus status, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(AttendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getLateAttendances(LocalDate date) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return attendanceRepository.findLateAttendances(companyId, date)
                .stream()
                .map(AttendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAbsentees(LocalDate date) {
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
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void approveAttendance(Long id, String approverName) {
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
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.softDelete();
        attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse createManual(AttendanceRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .companyId(companyId)
                .employee(employee)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .shiftType(request.getShiftType())
                .overtimeHours(request.getOvertimeHours())
                .isLate(request.isLate())
                .lateMinutes(request.getLateMinutes())
                .isVerified(true)
                .build();

        // Calculate total hours
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            BigDecimal totalHours = attendance.calculateTotalHours();
            attendance.setTotalWorkingHours(totalHours);
        }

        attendance = attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }
}