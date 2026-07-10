package com.businessos.modules.support.sla;

import com.businessos.modules.support.ticket.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAPolicyRequest {

    @NotNull(message = "Policy name is required")
    private String policyName;

    private String description;

    @NotNull(message = "Applicable priority is required")
    private TicketPriority applicablePriority;

    private int firstResponseTimeHours;
    private int resolutionTimeHours;
    private boolean businessHoursOnly;
    private boolean active;
    private String notes;
}
