package com.businessos.modules.finance.generalledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {

    Page<GeneralLedger> findByCompanyIdAndAccountId(Long companyId, Long accountId, Pageable pageable);

    Page<GeneralLedger> findByCompanyIdAndTransactionDateBetween(Long companyId, LocalDate start, LocalDate end, Pageable pageable);

    Page<GeneralLedger> findByCompanyId(Long companyId, Pageable pageable);

    List<GeneralLedger> findByCompanyIdAndReferenceTypeAndReferenceId(Long companyId, String type, Long id);

    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.companyId = :companyId AND gl.transactionDate BETWEEN :start AND :end")
    List<GeneralLedger> findTransactionsBetweenDates(@Param("companyId") Long companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}