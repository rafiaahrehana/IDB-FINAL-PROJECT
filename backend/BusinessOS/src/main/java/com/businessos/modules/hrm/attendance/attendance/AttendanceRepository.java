package com.businessos.modules.hrm.attendance.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Tenant-scoped lookup — used for all single-record operations to prevent
     * cross-tenant data access.
     */
    Optional<Attendance> findByIdAndCompanyId(Long id, Long companyId);

    /**
     * Duplicate check — ensures only one attendance record per employee per day.
     */
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    /**
     * Employee's own attendance history (paginated).
     */
    Page<Attendance> findByCompanyIdAndEmployeeId(Long companyId, Long employeeId, Pageable pageable);

    /**
     * Admin: all attendance in the company for a date range.
     */
    Page<Attendance> findByCompanyIdAndAttendanceDateBetween(
        Long companyId, LocalDate start, LocalDate end, Pageable pageable
    );

    /**
     * Admin: filter by attendance status.
     */
    Page<Attendance> findByCompanyIdAndStatus(Long companyId, AttendanceStatus status, Pageable pageable);

    /**
     * Admin: employees with ABSENT status on a specific date.
     */
    List<Attendance> findByCompanyIdAndStatusAndAttendanceDateBetween(
        Long companyId, AttendanceStatus status, LocalDate start, LocalDate end
    );

    /**
     * Admin: employees who were late on a specific date.
     * Uses JPQL enum comparison (not string literal) for type safety.
     */
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.companyId = :companyId
          AND a.attendanceDate = :date
          AND a.status = com.businessos.modules.hrm.attendance.attendance.AttendanceStatus.LATE
          AND a.deleted = false
        """)
    List<Attendance> findLateAttendances(
        @Param("companyId") Long companyId,
        @Param("date") LocalDate date
    );

    /**
     * Employee date range query — used in reports.
     */
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.employee.id = :employeeId
          AND a.attendanceDate BETWEEN :start AND :end
          AND a.deleted = false
        """)
    List<Attendance> findByEmployeeAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Dashboard: count attendance records by status for a specific company and date.
     */
    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.companyId = :companyId
          AND a.status = :status
          AND a.attendanceDate = :date
          AND a.deleted = false
        """)
    long countByCompanyIdAndStatusAndDate(
        @Param("companyId") Long companyId,
        @Param("status") AttendanceStatus status,
        @Param("date") LocalDate date
    );

    /**
     * Employee-level count — used in summary reports (e.g. how many days present in a month).
     */
    long countByEmployeeIdAndStatusAndAttendanceDateBetween(
        Long employeeId, AttendanceStatus status, LocalDate start, LocalDate end
    );

    /**
     * Bulk update: mark all employees with no attendance record for today as ABSENT.
     * Used by the DailyAbsenteeScheduler.
     */
    @Modifying
    @Query("""
        UPDATE Attendance a SET a.status = :absent
        WHERE a.companyId = :companyId
          AND a.attendanceDate = :date
          AND a.status = :unmarked
          AND a.deleted = false
        """)
    int bulkMarkAbsent(
        @Param("companyId") Long companyId,
        @Param("date") LocalDate date,
        @Param("absent") AttendanceStatus absent,
        @Param("unmarked") AttendanceStatus unmarked
    );
}