package com.businessos.modules.hrm.performance;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PerformanceReviewResponse {
    private Long id;
    private LocalDate reviewPeriodStart;
    private LocalDate reviewPeriodEnd;
    private Integer scoreWorkQuality;
    private Integer scoreProductivity;
    private Integer scoreCommunication;
    private Integer scoreTeamwork;
    private Integer scoreInitiative;
    private Integer scorePunctuality;
    private Double overallScore;
    private String strengths;
    private String areasForImprovement;
    private String goalsForNextPeriod;
    private String comments;
    private boolean finalised;
    private Long employeeId;
    private String employeeName;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private String aiSummary;
}
