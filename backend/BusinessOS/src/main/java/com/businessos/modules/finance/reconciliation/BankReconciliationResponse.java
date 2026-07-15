package com.businessos.modules.finance.reconciliation;

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
public class BankReconciliationResponse {
    private Long id;
    private Long companyId;
    
    private Long bankAccountId;
    private String bankAccountName;
    
    private LocalDate reconciliationDate;
    
    private BigDecimal glBalance;
    private BigDecimal bankStatementBalance;
    private BigDecimal difference;
    
    private String outstandingDeposits;
    private String outstandingChecks;
    
    private boolean reconciled;
    private LocalDate reconciledDate;
    private String reconciledBy;
    private String discrepancyNotes;
}
