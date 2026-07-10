package com.businessos.modules.hrm.attendance.shift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {

    Optional<EmployeeShiftAssignment> findByIdAndCompanyId(Long id, Long companyId);

    @Query("""
            SELECT esa FROM EmployeeShiftAssignment esa
            WHERE esa.companyId = :companyId
              AND esa.employee.id = :employeeId
              AND esa.active = true
              AND esa.deleted = false
            """)
    Optional<EmployeeShiftAssignment> findByCompanyIdAndEmployeeIdAndActive(@Param("companyId") Long companyId, @Param("employeeId") Long employeeId);

    Page<EmployeeShiftAssignment> findByCompanyIdAndShiftIdAndActiveTrue(Long companyId, Long shiftId, Pageable pageable);

    Page<EmployeeShiftAssignment> findByCompanyId(Long companyId, Pageable pageable);
}
