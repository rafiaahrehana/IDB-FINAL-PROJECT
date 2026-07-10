package com.businessos.modules.finance.generalledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedgerResponse {
    private Long id;
    private Long companyId;
    private LocalDate transactionDate;
    private Long accountId;
    private String accountName;
    private String accountCode;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String description;
    private String referenceType;
    private Long referenceId;
    private String referenceNumber;
    private boolean isReconciled;
    private String reconciliationNotes;
    private String postedBy;
    private LocalDate postedDate;
    private boolean posted;
}
