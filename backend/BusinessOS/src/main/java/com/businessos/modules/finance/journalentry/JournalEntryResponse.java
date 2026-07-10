package com.businessos.modules.finance.journalentry;

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
public class JournalEntryResponse {
    private Long id;
    private Long companyId;
    private String journalEntryNumber;
    private LocalDate entryDate;
    
    private Long debitAccountId;
    private String debitAccountName;
    
    private Long creditAccountId;
    private String creditAccountName;
    
    private BigDecimal amount;
    private String description;
    private String notes;
    
    private String createdBy;
    private LocalDate createdDate;
    
    private String approvedBy;
    private LocalDate approvedDate;
    private boolean approved;
    
    private boolean posted;
    private LocalDate postedDate;
}
