package com.businessos.modules.finance.reconciliation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, Long> {
    
    Page<BankReconciliation> findByCompanyId(Long companyId, Pageable pageable);
    
    List<BankReconciliation> findByCompanyIdAndReconciledFalse(Long companyId);
}
