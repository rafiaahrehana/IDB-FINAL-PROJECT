package com.businessos.modules.dashboard;

import com.businessos.modules.hrm.announcement.AnnouncementResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

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

    //  HRM / Projects / Tasks / Meetings
    long totalEmployees;
    long activeProjects;
    long openTasks;
    long meetingsToday;
    double projectProgress;

    // Task status breakdown (for chart)
    long taskPending;
    long taskInProgress;
    long taskCompleted;
    long taskBlocked;
    long taskCancelled;

    // Active announcements for the company
    List<AnnouncementResponse> announcements;
}
