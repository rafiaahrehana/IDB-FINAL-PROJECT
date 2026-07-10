package com.businessos.modules.hrm.performance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PerformanceReviewRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    @NotNull
    private LocalDate reviewPeriodStart;
    @NotNull
    private LocalDate reviewPeriodEnd;
    @Min(1) @Max(5)
    private Integer scoreWorkQuality;
    @Min(1) @Max(5)
    private Integer scoreProductivity;
    @Min(1) @Max(5)
    private Integer scoreCommunication;
    @Min(1) @Max(5)
    private Integer scoreTeamwork;
    @Min(1) @Max(5)
    private Integer scoreInitiative;
    @Min(1) @Max(5)
    private Integer scorePunctuality;
    private String strengths;
    private String areasForImprovement;
    private String goalsForNextPeriod;
    private String comments;
}
