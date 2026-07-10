package com.businessos.modules.finance.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("financeExpenseRepository")
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByCompanyIdAndExpenseNumber(Long companyId, String number);

    Page<Expense> findByCompanyIdAndStatus(Long companyId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByCompanyIdAndSubmittedByIdAndStatus(Long companyId, Long employeeId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByCompanyId(Long companyId, Pageable pageable);

    List<Expense> findByCompanyIdAndExpenseDateBetween(Long companyId, LocalDate start, LocalDate end);

    Page<Expense> findByCompanyIdAndVendorName(Long companyId, String vendorName, Pageable pageable);
}
