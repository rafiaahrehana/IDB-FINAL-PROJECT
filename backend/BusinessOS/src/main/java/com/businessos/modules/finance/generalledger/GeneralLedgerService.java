package com.businessos.modules.finance.generalledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GeneralLedgerService {

    void recordTransaction(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                           String description, GlReferenceType referenceType, Long referenceId,
                           String referenceNumber);

    /**
     * Same as recordTransaction() but with an explicit companyId instead of resolving
     * it from the security context - required by system entry points that have no
     * authenticated user (e.g. the SSLCommerz success/IPN callbacks, which are public
     * endpoints per SecurityConfig). Callers that already have a companyId in hand
     * (the domain entity being posted, or a companyId parameter) should prefer this
     * overload even when a security context does happen to be present.
     */
    void recordTransaction(Long companyId, Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                           String description, GlReferenceType referenceType, Long referenceId,
                           String referenceNumber);

    GeneralLedgerResponse getById(Long id);

    Page<GeneralLedgerResponse> getAll(Pageable pageable);

    Page<GeneralLedgerResponse> getByAccount(Long accountId, Pageable pageable);

    Page<GeneralLedgerResponse> getByDateRange(LocalDate start, LocalDate end, Pageable pageable);

    List<GeneralLedgerResponse> getByReference(GlReferenceType referenceType, Long referenceId);

    void reconcile(Long id, String notes);

    BigDecimal getAccountBalance(Long accountId);
}
