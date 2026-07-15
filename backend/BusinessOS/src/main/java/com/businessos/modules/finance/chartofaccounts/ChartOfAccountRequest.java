package com.businessos.modules.finance.chartofaccounts;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    // Matches ChartOfAccountResponse.isHeaderAccount naming/JSON key - see the comment there.
    @JsonProperty("isHeaderAccount")
    private boolean isHeaderAccount;
    @Builder.Default
    private boolean allowDirectPosting = true;
    @Builder.Default
    private boolean active = true;
    private String notes;
}
