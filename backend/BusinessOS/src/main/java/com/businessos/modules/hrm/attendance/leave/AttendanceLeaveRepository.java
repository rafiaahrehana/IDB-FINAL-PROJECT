package com.businessos.modules.hrm.attendance.leave;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLeaveRepository extends JpaRepository<AttendanceLeave, Long> {
    Page<AttendanceLeave> findByCompanyId(Long companyId, Pageable pageable);
    Page<AttendanceLeave> findByCompanyIdAndEmployeeId(Long companyId, Long employeeId, Pageable pageable);
    List<AttendanceLeave> findByCompanyIdAndApprovedFalse(Long companyId);
    Page<AttendanceLeave> findByCompanyIdAndLeaveDateBetween(Long companyId, LocalDate start, LocalDate end, Pageable pageable);
}
