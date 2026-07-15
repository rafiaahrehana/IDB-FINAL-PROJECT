package com.businessos.core.scheduler;

import com.businessos.enums.LeaveRequestStatus;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.hrm.attendance.attendance.Attendance;
import com.businessos.modules.hrm.attendance.attendance.AttendanceRepository;
import com.businessos.modules.hrm.attendance.attendance.AttendanceStatus;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignment;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignmentRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.hrm.leave.holiday.HolidayRepository;
import com.businessos.modules.hrm.leave.leaverequest.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Marks employees ABSENT when they never checked in / never had attendance
 * recorded for the day - "no check-in or check-out" no longer means silence,
 * it produces a real ABSENT record. Skips company holidays, each employee's
 * weekly off day (from their assigned shift), and employees on approved leave.
 * Runs daily at 19:00, after the default office day ends.
 */
@Component
@RequiredArgsConstructor
public class DailyAbsenteeScheduler {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeShiftAssignmentRepository shiftAssignmentRepository;

    @Scheduled(cron = "0 0 19 * * *")
    @Transactional
    public void markAbsentees() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        for (Company company : companyRepository.findAll()) {
            if (company.isPlatformTenant()) continue;
            if (holidayRepository.existsByCompanyIdAndDate(company.getId(), today)) continue;

            for (Employee employee : employeeRepository.findByCompanyIdAndActiveTrue(company.getId())) {
                if (attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today).isPresent()) {
                    continue; // already has a record today (checked in, or manually entered)
                }
                if (isWeeklyOff(company.getId(), employee.getId(), dayOfWeek)) continue;
                if (leaveRequestRepository.existsApprovedForEmployeeAndDate(
                        employee.getId(), today, LeaveRequestStatus.APPROVED)) continue;

                Attendance attendance = Attendance.builder()
                        .companyId(company.getId())
                        .employee(employee)
                        .attendanceDate(today)
                        .status(AttendanceStatus.ABSENT)
                        .build();
                attendanceRepository.save(attendance);
            }
        }
    }

    private boolean isWeeklyOff(Long companyId, Long employeeId, DayOfWeek dayOfWeek) {
        EmployeeShiftAssignment assignment = shiftAssignmentRepository
                .findByCompanyIdAndEmployeeIdAndActive(companyId, employeeId)
                .orElse(null);
        String weeklyOffDays = assignment != null ? assignment.getShift().getWeeklyOffDays() : "FRI,SAT";
        if (weeklyOffDays == null || weeklyOffDays.isBlank()) return false;

        String abbreviation = dayOfWeek.name().substring(0, 3); // MONDAY -> MON
        List<String> offDays = Arrays.asList(weeklyOffDays.split(","));
        return offDays.contains(abbreviation);
    }
}
