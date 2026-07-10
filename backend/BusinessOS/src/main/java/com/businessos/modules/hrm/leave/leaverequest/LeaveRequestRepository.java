package com.businessos.modules.hrm.leave.leaverequest;

import com.businessos.enums.LeaveRequestStatus;
import com.businessos.enums.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Optional<LeaveRequest> findByIdAndCompanyId(Long id, Long companyId);

    Page<LeaveRequest> findByCompanyId(Long companyId, Pageable pageable);

    Page<LeaveRequest> findByCompanyIdAndStatus(
        Long companyId, LeaveRequestStatus status, Pageable pageable);

    Page<LeaveRequest> findByCompanyIdAndEmployeeId(
        Long companyId, Long employeeId, Pageable pageable);

    @Query("""
        SELECT SUM(lr.totalDays) FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.leaveType = :leaveType
          AND lr.status = :status
          AND YEAR(lr.startDate) = :year
          AND lr.deleted = false
        """)
    Optional<Integer> sumDaysByEmployeeAndTypeAndStatusAndYear(
        @Param("employeeId") Long employeeId,
        @Param("leaveType") LeaveType leaveType,
        @Param("status") LeaveRequestStatus status,
        @Param("year") int year);

    @Query("""
        SELECT COUNT(lr) > 0 FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.status NOT IN :excludedStatuses
          AND lr.startDate <= :endDate
          AND lr.endDate >= :startDate
          AND lr.deleted = false
        """)
    boolean hasOverlappingLeave(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludedStatuses") java.util.List<LeaveRequestStatus> excludedStatuses);
}
