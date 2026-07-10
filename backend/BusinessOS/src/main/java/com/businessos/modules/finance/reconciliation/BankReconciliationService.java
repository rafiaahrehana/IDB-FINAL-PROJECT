package com.businessos.modules.finance.reconciliation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BankReconciliationService {

    BankReconciliationResponse create(BankReconciliationRequest request);

    BankReconciliationResponse getById(Long id);

    Page<BankReconciliationResponse> getAll(Pageable pageable);

    void markAsReconciled(Long id, String notes);

    List<BankReconciliation> getPendingReconciliations();
}
