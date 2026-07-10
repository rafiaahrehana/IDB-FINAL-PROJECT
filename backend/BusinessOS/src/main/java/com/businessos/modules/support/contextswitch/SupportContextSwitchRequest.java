package com.businessos.modules.support.contextswitch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportContextSwitchRequest {

    @NotNull(message = "Support agent ID is required")
    private Long supportAgentId;

    @NotNull(message = "Company ID is required")
    private Long viewedCompanyId;

    private String purpose;
    private String ipAddress;
}
