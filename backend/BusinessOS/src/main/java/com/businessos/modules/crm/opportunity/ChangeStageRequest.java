package com.businessos.modules.crm.opportunity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeStageRequest {

    @NotNull(message = "Stage is required")
    private OpportunityStage stage;

    // Required when moving to CLOSED_LOST
    @Size(max = 255)
    private String lostReason;
}
