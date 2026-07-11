package com.businessos.modules.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @GetMapping("/recommendations")
    public ResponseEntity<java.util.List<RecommendationResponse>> getRecommendations() {
        return ResponseEntity.ok(dashboardService.getRecommendations());
    }

    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @GetMapping("/insights")
    public ResponseEntity<InsightsResponse> getAiInsights() {
        return ResponseEntity.ok(dashboardService.getAiInsights());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_AGENT', 'SUPPORT_MANAGER', 'MARKETING_MANAGER', 'PLATFORM_ACCOUNTANT', 'SALES_MANAGER')")
    @GetMapping("/platform-summary")
    public ResponseEntity<PlatformSummaryResponse> getPlatformSummary() {
        return ResponseEntity.ok(dashboardService.getPlatformSummary());
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client-summary")
    public ResponseEntity<ClientSummaryResponse> getClientSummary() {
        return ResponseEntity.ok(dashboardService.getClientSummary());
    }
}
