package com.businessos.modules.hrm.attendance.reports.service;

import com.businessos.modules.hrm.attendance.attendance.Attendance;
import com.businessos.modules.hrm.attendance.attendance.AttendanceStatus;
import com.businessos.modules.hrm.attendance.attendance.AttendanceRepository;
import com.businessos.modules.hrm.attendance.reports.dto.*;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.auth.user.User;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceReportServiceImpl implements AttendanceReportService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public DailyAttendanceReport generateDailyReport(LocalDate date) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();

        List<Attendance> attendances = attendanceRepository
                .findByCompanyIdAndAttendanceDateBetween(companyId, date, date,
                        org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();

        long presentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long lateCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long onLeaveCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE)
                .count();

        return DailyAttendanceReport.builder()
                .reportDate(date)
                .totalEmployees((long) attendances.size())
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .onLeaveCount(onLeaveCount)
                .attendancePercentage((presentCount * 100) / (long) attendances.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceReport generateMonthlyReport(int month, int year) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Attendance> attendances = attendanceRepository
                .findByCompanyIdAndAttendanceDateBetween(companyId, startDate, endDate,
                        org.springframework.data.domain.PageRequest.of(0, 100000)).getContent();

        long presentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long lateCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        return MonthlyAttendanceReport.builder()
                .month(month)
                .year(year)
                .totalWorkingDays(calculateWorkingDays(startDate, endDate))
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .attendancePercentage(calculatePercentage(presentCount, attendances.size()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAttendanceSummary generateEmployeeSummary(Long employeeId, LocalDate start, LocalDate end) {
        if (!authorizationService.hasPermission(PermissionCode.ATTENDANCE_VIEW)) {
            User currentUser = securityUtil.getCurrentUser();
            Employee currentEmployee = currentUser != null
                    ? employeeRepository.findByUserId(currentUser.getId()).orElse(null)
                    : null;
            if (currentEmployee == null || employeeId == null || !currentEmployee.getId().equals(employeeId)) {
                throw new ForbiddenException("Access denied: you can only view your own attendance summary");
            }
        }
        List<Attendance> attendances = attendanceRepository
                .findByEmployeeAndDateRange(employeeId, start, end);

        long presentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long lateCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        return EmployeeAttendanceSummary.builder()
                .employeeId(employeeId)
                .periodStart(start)
                .periodEnd(end)
                .presentDays(presentCount)
                .lateDays(lateCount)
                .absentDays(absentCount)
                .attendancePercentage(calculatePercentage(presentCount, attendances.size()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentAttendanceReport generateDepartmentReport(String department, LocalDate date) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        List<Employee> employees = employeeRepository.findByDepartment(department);

        long presentCount = 0;
        long lateCount = 0;
        long absentCount = 0;

        for (Employee emp : employees) {
            List<Attendance> att = attendanceRepository
                    .findByEmployeeAndDateRange(emp.getId(), date, date);

            for (Attendance a : att) {
                if (a.getStatus() == AttendanceStatus.PRESENT) presentCount++;
                else if (a.getStatus() == AttendanceStatus.LATE) lateCount++;
                else if (a.getStatus() == AttendanceStatus.ABSENT) absentCount++;
            }
        }

        return DepartmentAttendanceReport.builder()
                .department(department)
                .reportDate(date)
                .totalEmployees((long) employees.size())
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .attendancePercentage(calculatePercentage(presentCount, employees.size()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LateAndAbsentReport generateLateAbsentReport(LocalDate date) {
        authorizationService.checkPermission(PermissionCode.ATTENDANCE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();

        List<Attendance> lateAttendances = attendanceRepository
                .findLateAttendances(companyId, date);

        List<Attendance> absentAttendances = attendanceRepository
                .findByCompanyIdAndStatusAndAttendanceDateBetween(companyId, AttendanceStatus.ABSENT, date, date);

        return LateAndAbsentReport.builder()
                .reportDate(date)
                .lateCount((long) lateAttendances.size())
                .absentCount((long) absentAttendances.size())
                .build();
    }

    private long calculateWorkingDays(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }

    private double calculatePercentage(long count, long total) {
        return total > 0 ? (count * 100.0) / total : 0.0;
    }
}
