package com.businessos.modules.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class PlatformSummaryResponse {

    // Companies by lifecycle status
    long totalCompanies;
    long activeCompanies;
    long trialCompanies;
    long suspendedCompanies;
    long pendingVerificationCompanies;

    // Trials whose subscription window ends within the next 7 days
    long trialsExpiringWithin7Days;

    // Companies by subscription plan
    long freePlanCompanies;
    long starterPlanCompanies;
    long proPlanCompanies;
    long enterprisePlanCompanies;

    // SaaS staff accounts (all platform roles)
    long totalPlatformUsers;

    // Recorded subscription revenue (SUM of SubscriptionHistory.amountPaid)
    double totalRevenue;
    double revenueThisMonth;
}
