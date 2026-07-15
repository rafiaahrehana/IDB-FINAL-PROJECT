package com.businessos.modules.dashboard;

import java.time.LocalDate;

public interface DashboardService {
    DashboardSummaryResponse getSummary(LocalDate from, LocalDate to);

    java.util.List<RecommendationResponse> getRecommendations();

    InsightsResponse getAiInsights();

    PlatformSummaryResponse getPlatformSummary();

    ClientSummaryResponse getClientSummary();
}
