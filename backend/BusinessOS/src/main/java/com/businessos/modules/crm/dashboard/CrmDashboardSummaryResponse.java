package com.businessos.modules.crm.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmDashboardSummaryResponse {
    private BigDecimal pipelineValue;
    private BigDecimal wonThisMonth;
    private long qualifiedLeadsCount;
    private double conversionRate; // percent, 0-100
    private List<UpcomingFollowUp> upcomingFollowUps;

    private long totalClients;
    private long totalLeads;
    private long totalOpportunities;
    private long openOpportunitiesCount;
    private long wonCount;
    private BigDecimal wonValue;
    private long lostCount;
    private BigDecimal lostValue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpcomingFollowUp {
        private Long activityId;
        private String subject;
        private LocalDateTime followUpAt;
        private String relatedName;
    }
}
