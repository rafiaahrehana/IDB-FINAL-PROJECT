package com.businessos.modules.hrm.salary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    Optional<SalaryStructure> findByIdAndCompanyId(Long id, Long companyId);

    Page<SalaryStructure> findByCompanyIdAndEmployeeId(Long companyId, Long employeeId, Pageable pageable);

    /**
     * Returns the currently active structure — effectiveTo IS NULL.
     */
    Optional<SalaryStructure> findByEmployeeIdAndEffectiveToIsNull(Long employeeId);

    @Query("""
        SELECT s FROM SalaryStructure s
        WHERE s.employee.id = :employeeId
          AND s.effectiveFrom <= :date
          AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
          AND s.deleted = false
        """)
    Optional<SalaryStructure> findActiveForEmployeeOnDate(
        @Param("employeeId") Long employeeId,
        @Param("date") LocalDate date);

    List<SalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
}
