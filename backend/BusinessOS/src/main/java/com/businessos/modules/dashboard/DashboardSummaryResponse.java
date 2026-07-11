package com.businessos.modules.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @Builder
public class DashboardSummaryResponse {

    // CRM
    long totalLeads;
    long newLeads;
    long qualifiedLeads;
    long totalClients;
    long openOpportunities;
    BigDecimal pipelineValue;
    BigDecimal weightedForecast;

    // Servicedesk
    long pendingRequests;
    long inProgressRequests;
    long completedRequestsAllTime;
    long slaBreachedOpen;

    //  Support tickets
    long openTickets;
    long newTickets;

    //  Finance
    BigDecimal outstandingInvoiceAmount;
    BigDecimal walletBalance;
    BigDecimal walletCreditBalance;
}
