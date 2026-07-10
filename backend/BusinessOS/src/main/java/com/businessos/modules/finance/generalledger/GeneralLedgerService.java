package com.businessos.modules.finance.generalledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GeneralLedgerService {

    void recordTransaction(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                           String description, String referenceType, Long referenceId,
                           String referenceNumber);

    GeneralLedgerResponse getById(Long id);

    Page<GeneralLedgerResponse> getAll(Pageable pageable);

    Page<GeneralLedgerResponse> getByAccount(Long accountId, Pageable pageable);

    Page<GeneralLedgerResponse> getByDateRange(LocalDate start, LocalDate end, Pageable pageable);

    List<GeneralLedgerResponse> getByReference(String referenceType, Long referenceId);

    void reconcile(Long id, String notes);

    BigDecimal getAccountBalance(Long accountId);
}
