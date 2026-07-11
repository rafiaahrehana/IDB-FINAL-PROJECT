package com.businessos.modules.hrm.payroll;

import com.businessos.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Payroll> findByEmployeeIdAndPayMonthAndPayYear(
        Long employeeId, int payMonth, int payYear);

    Page<Payroll> findByCompanyId(Long companyId, Pageable pageable);

    Page<Payroll> findByCompanyIdAndPayMonthAndPayYear(
        Long companyId, int payMonth, int payYear, Pageable pageable);

    Page<Payroll> findByCompanyIdAndEmployeeId(
        Long companyId, Long employeeId, Pageable pageable);

    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.company.id = :companyId AND p.payMonth = :month AND p.payYear = :year AND p.status = :status AND p.deleted = false")
    Optional<BigDecimal> sumNetSalaryByCompanyAndPeriod(
        @Param("companyId") Long companyId,
        @Param("month") int month,
        @Param("year") int year,
        @Param("requeststatus") PayrollStatus status);
}
