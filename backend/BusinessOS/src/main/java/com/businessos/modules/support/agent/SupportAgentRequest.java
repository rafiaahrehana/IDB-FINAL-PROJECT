package com.businessos.modules.support.agent;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
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
