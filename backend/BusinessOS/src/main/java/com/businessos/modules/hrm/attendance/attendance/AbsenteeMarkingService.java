package com.businessos.modules.hrm.attendance.attendance;

import com.businessos.enums.LeaveRequestStatus;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignment;
import com.businessos.modules.hrm.attendance.shift.EmployeeShiftAssignmentRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.hrm.leave.holiday.HolidayRepository;
import com.businessos.modules.hrm.leave.leaverequest.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Creates ABSENT attendance records for employees who never checked in on a
 * working day. Shared by {@code DailyAbsenteeScheduler} (nightly backfill) and
 * the manual admin backfill endpoint.
 *
 * Every marking pass is idempotent: a date that already has any record for an
 * employee (check-in, manual entry, or a prior ABSENT) is left untouched, and
 * these are all skipped — company holidays, the employee's weekly-off day (from
 * their assigned shift), approved leave, future dates, and any date before the
 * employee's hire date.
 */
@Service
@RequiredArgsConstructor
public class AbsenteeMarkingService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * Mark absentees for one date across every tenant company. Public so the
     * scheduler can call it once per date through the Spring proxy, giving each
     * date its own transaction — a failure or downtime on one day never skips or
     * rolls back the others.
     *
     * @return number of ABSENT records created
     */
    @Transactional
    public int markAllCompaniesForDate(LocalDate targetDate) {
        int created = 0;
        for (Company company : companyRepository.findAll()) {
            if (company.isPlatformTenant()) continue;
            created += markCompanyForDate(company, targetDate);
        }
        return created;
    }

    /**
     * Backfill absentees for a single company across an inclusive date range —
     * used by the tenant-scoped manual admin trigger.
     *
     * @return number of ABSENT records created
     */
    @Transactional
    public int backfillForCompany(Long companyId, LocalDate start, LocalDate end) {
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null || company.isPlatformTenant()) return 0;

        int created = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            created += markCompanyForDate(company, d);
        }
        return created;
    }

    private int markCompanyForDate(Company company, LocalDate targetDate) {
        // Never mark absence for a day that hasn't happened yet.
        if (targetDate.isAfter(LocalDate.now())) return 0;
        if (holidayRepository.existsByCompanyIdAndDate(company.getId(), targetDate)) return 0;

        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
        int created = 0;

        for (Employee employee : employeeRepository.findByCompanyIdAndActiveTrue(company.getId())) {
            // Don't invent absences for days before the employee joined.
            if (employee.getHireDate() != null && targetDate.isBefore(employee.getHireDate())) continue;
            // Already has a record for this day (checked in, manual entry, or prior ABSENT).
            if (!attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), targetDate).isEmpty()) continue;
            if (isWeeklyOff(company.getId(), employee.getId(), dayOfWeek)) continue;
            if (leaveRequestRepository.existsApprovedForEmployeeAndDate(
                    employee.getId(), targetDate, LeaveRequestStatus.APPROVED)) continue;

            attendanceRepository.save(Attendance.builder()
                    .companyId(company.getId())
                    .employee(employee)
                    .attendanceDate(targetDate)
                    .status(AttendanceStatus.ABSENT)
                    .build());
            created++;
        }
        return created;
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
