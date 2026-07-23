package com.businessos.modules.support.agent;

import jakarta.validation.constraints.*;
import lombok.*;

// AllArgsConstructor access is package-private: see ChartOfAccountRequest for why -
// a public one is picked up by Jackson as a deserialization creator, which fails on
// any missing primitive field instead of defaulting it.
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PACKAGE) @Builder
public class SupportAgentRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String department;
    private String specialization;

    @NotNull(message = "Status is required")
    private SupportAgentStatus status;

    @Min(value = 1)
    @Builder.Default
    private int maxConcurrentTickets = 10;

    private String notes;
}
