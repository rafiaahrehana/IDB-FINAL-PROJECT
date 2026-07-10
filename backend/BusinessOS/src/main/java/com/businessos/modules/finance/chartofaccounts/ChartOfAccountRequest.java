package com.businessos.modules.finance.chartofaccounts;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChartOfAccountRequest {

    @NotBlank(message = "Account code is required")
    @Size(min = 3, max = 10)
    private String accountCode;

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotNull(message = "Account type is required")
    private AccountType type;

    private String description;
    private boolean headerAccount;
    @Builder.Default
    private boolean allowDirectPosting = true;
    private String notes;
}
