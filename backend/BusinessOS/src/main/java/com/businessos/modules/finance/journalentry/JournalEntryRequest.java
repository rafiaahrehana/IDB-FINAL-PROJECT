package com.businessos.modules.finance.journalentry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class JournalEntryRequest {

    @NotNull(message = "Debit account ID is required")
    private Long debitAccountId;

    @NotNull(message = "Credit account ID is required")
    private Long creditAccountId;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
    
    private String notes;
}
