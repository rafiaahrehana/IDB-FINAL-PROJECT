package com.businessos.modules.finance.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("financeExpenseRepository")
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Expense> findByCompanyIdAndExpenseNumber(Long companyId, String number);

    /**
     * Used by ExpenseServiceImpl.generateExpenseNumber - MAX-based (not COUNT) to be
     * safe against concurrent inserts and soft-deleted records skewing the sequence,
     * and scoped per company (a global count() previously let expense numbers collide
     * across different companies and skip/duplicate under concurrent submissions).
     */
    @Query("SELECT MAX(e.expenseNumber) FROM FinanceExpense e WHERE e.companyId = :companyId AND e.expenseNumber LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxExpenseNumberByCompanyAndPrefix(@Param("companyId") Long companyId, @Param("prefix") String prefix);

    Page<Expense> findByCompanyIdAndStatus(Long companyId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByCompanyIdAndSubmittedByIdAndStatus(Long companyId, Long employeeId, ExpenseStatus status, Pageable pageable);
    Page<Expense> findByCompanyIdAndSubmittedById(Long companyId, Long employeeId, Pageable pageable);

    Page<Expense> findByCompanyId(Long companyId, Pageable pageable);

    List<Expense> findByCompanyIdAndExpenseDateBetween(Long companyId, LocalDate start, LocalDate end);

    Page<Expense> findByCompanyIdAndVendorName(Long companyId, String vendorName, Pageable pageable);
}
