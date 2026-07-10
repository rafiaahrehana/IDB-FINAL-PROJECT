package com.businessos.modules.finance.chartofaccounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartOfAccountResponse {
    private Long id;
    private Long companyId;
    private String accountCode;
    private String accountName;
    private AccountType type;
    private BigDecimal balance;
    private boolean isHeaderAccount;
    private boolean allowDirectPosting;
    private boolean active;
    private String description;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
