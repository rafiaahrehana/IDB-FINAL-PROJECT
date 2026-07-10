package com.businessos.modules.dashboard;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    //Rule-based smart recommendations across modules (stale deals, SLA risk, expiring licenses, overdue invoices)
    java.util.List<RecommendationResponse> getRecommendations();

    // AI-generated business insights based on the dashboard summary
    InsightsResponse getAiInsights();
}
