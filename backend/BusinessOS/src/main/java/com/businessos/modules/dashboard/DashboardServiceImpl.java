package com.businessos.modules.dashboard;

import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.prompt.BusinessInsightsPromptBuilder;
import com.businessos.modules.ai.service.AiService;
import com.businessos.auth.role.enums.Role;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.enums.CompanyStatus;
import com.businessos.enums.InvoiceStatus;
import com.businessos.enums.LeadStatus;
import com.businessos.enums.ServiceRequestStatus;
import com.businessos.enums.SubscriptionPlan;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.crm.lead.LeadRepository;
import com.businessos.modules.crm.opportunity.OpportunityRepository;
import com.businessos.modules.crm.opportunity.OpportunityStage;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequestRepository;
import com.businessos.modules.itam.software.SoftwareLicenseRepository;
import com.businessos.modules.support.ticket.SupportTicketRepository;
import com.businessos.modules.support.ticket.TicketStatus;
import com.businessos.modules.finance.invoice.ClientInvoiceRepository;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.shared.payment.wallet.Wallet;
import com.businessos.shared.payment.wallet.WalletRepository;
import com.businessos.shared.subscription.SubscriptionHistoryRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepository            leadRepository;
    private final ClientRepository          clientRepository;
    private final OpportunityRepository     opportunityRepository;
    private final ServiceRequestRepository  serviceRequestRepository;
    private final SupportTicketRepository   supportTicketRepository;
    private final ClientInvoiceRepository   invoiceRepository;
    private final WalletRepository          walletRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;
    private final CompanyRepository         companyRepository;
    private final UserRepository            userRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final AiService                 aiService;
    private final SecurityUtil              securityUtil;

    @Override
    public DashboardSummaryResponse getSummary(LocalDate from, LocalDate to) {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }

        // Period filter - only "new in period" metrics respect from/to; current-state
        // counts (open opportunities, pending requests, totals, wallet balance) and
        // completedRequestsAllTime stay all-time by design - a snapshot shouldn't change
        // based on a trailing window.
        LocalDateTime periodStart = from != null ? from.atStartOfDay() : null;
        LocalDateTime periodEnd = to != null ? to.atTime(23, 59, 59) : null;
        boolean hasPeriod = periodStart != null && periodEnd != null;

        // CRM
        long totalLeads       = leadRepository.countByCompanyId(companyId);
        long newLeads         = hasPeriod
            ? leadRepository.countByCompanyIdAndStatusAndCreatedAtBetween(companyId, LeadStatus.NEW, periodStart, periodEnd)
            : leadRepository.countByCompanyIdAndStatus(companyId, LeadStatus.NEW);
        long qualifiedLeads   = leadRepository.countByCompanyIdAndStatus(companyId, LeadStatus.QUALIFIED);
        long totalClients     = clientRepository.countByCompanyId(companyId);

        // Pipeline summary (reuses the existing projection query)
        var pipelineStages    = opportunityRepository.summarizePipeline(companyId);
        long openOpportunities = 0;
        BigDecimal pipelineValue = BigDecimal.ZERO;
        BigDecimal weightedForecast = BigDecimal.ZERO;
        for (var stage : pipelineStages) {
            if (!stage.getStage().isClosed()) {
                openOpportunities += stage.getDealCount();
                pipelineValue = pipelineValue.add(stage.getTotalAmount() != null ? stage.getTotalAmount() : BigDecimal.ZERO);
                weightedForecast = weightedForecast.add(stage.getWeightedAmount() != null ? stage.getWeightedAmount() : BigDecimal.ZERO);
            }
        }

        // Servicedesk
        long pendingRequests    = serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.PENDING)
                                + serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.QUOTATION_PENDING);
        long inProgressRequests = serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.IN_PROGRESS)
                                + serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.ASSIGNED);
        long completedAllTime   = serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.COMPLETED);
        long slaBreached        = serviceRequestRepository.countByCompanyIdAndSlaBreachTrueAndStatusNotIn(
                                    companyId, List.of(ServiceRequestStatus.COMPLETED,
                                                       ServiceRequestStatus.CANCELLED,
                                                       ServiceRequestStatus.REJECTED));

        // Support tickets
        long openTickets = supportTicketRepository.countByStatusAndCompanyId(TicketStatus.OPEN, companyId)
                         + supportTicketRepository.countByStatusAndCompanyId(TicketStatus.IN_PROGRESS, companyId)
                         + supportTicketRepository.countByStatusAndCompanyId(TicketStatus.WAITING, companyId);
        long newTickets  = hasPeriod
            ? supportTicketRepository.countByCompanyIdAndStatusAndCreatedAtBetween(companyId, TicketStatus.NEW, periodStart, periodEnd)
            : supportTicketRepository.countByStatusAndCompanyId(TicketStatus.NEW, companyId);

        // Finance
        BigDecimal outstanding = invoiceRepository.sumOutstandingByCompanyId(
                companyId,
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE))
            .orElse(BigDecimal.ZERO);

        Wallet wallet = walletRepository.findByContextTypeAndContextId("COMPANY", companyId).orElse(null);
        BigDecimal walletBalance       = wallet != null ? wallet.getBalance()       : BigDecimal.ZERO;
        BigDecimal walletCreditBalance = wallet != null ? wallet.getCreditBalance() : BigDecimal.ZERO;

        return DashboardSummaryResponse.builder()
            .totalLeads(totalLeads)
            .newLeads(newLeads)
            .qualifiedLeads(qualifiedLeads)
            .totalClients(totalClients)
            .openOpportunities(openOpportunities)
            .pipelineValue(pipelineValue)
            .weightedForecast(weightedForecast)
            .pendingRequests(pendingRequests)
            .inProgressRequests(inProgressRequests)
            .completedRequestsAllTime(completedAllTime)
            .slaBreachedOpen(slaBreached)
            .openTickets(openTickets)
            .newTickets(newTickets)
            .outstandingInvoiceAmount(outstanding)
            .walletBalance(walletBalance)
            .walletCreditBalance(walletCreditBalance)
            .build();
    }

    // ==================== Smart Recommendations (rule-based) ====================

    @Override
    public List<RecommendationResponse> getRecommendations() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }
        List<RecommendationResponse> recommendations = new ArrayList<>();

        // 1. Stale open opportunities — no activity for 14+ days
        List<OpportunityStage> closedStages =
            List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST);
        var staleDeals = opportunityRepository.findStaleOpenOpportunities(
            companyId, closedStages, LocalDateTime.now().minusDays(14), PageRequest.of(0, 5));
        for (var deal : staleDeals) {
            recommendations.add(new RecommendationResponse(
                "FOLLOW_UP", "WARNING",
                "Opportunity \"" + deal.getName() + "\" has had no activity for over 14 days — follow up with "
                    + (deal.getClient() != null ? deal.getClient().getClientCompanyName() : "the client"),
                "/crm/pipeline"));
        }

        // 2. SLA-breached open service requests
        long slaBreached = serviceRequestRepository.countByCompanyIdAndSlaBreachTrueAndStatusNotIn(
            companyId, List.of(ServiceRequestStatus.COMPLETED,
                               ServiceRequestStatus.CANCELLED,
                               ServiceRequestStatus.REJECTED));
        if (slaBreached > 0) {
            recommendations.add(new RecommendationResponse(
                "SLA_RISK", "CRITICAL",
                slaBreached + " open service request(s) have breached their SLA — reassign or escalate now",
                "/servicedesk/requests"));
        }

        // 3. Software licenses expiring within 30 days
        var expiring = softwareLicenseRepository.findExpiringBetweenDates(
            companyId, LocalDate.now(), LocalDate.now().plusDays(30));
        for (var license : expiring) {
            recommendations.add(new RecommendationResponse(
                "LICENSE_EXPIRY", "WARNING",
                license.getSoftwareName() + " license expires on " + license.getLicenseExpiryDate()
                    + " — renew or cancel auto-billing",
                "/itam/software"));
        }

        // 4. Overdue invoices
        long overdue = invoiceRepository.countByCompanyIdAndStatus(companyId, InvoiceStatus.OVERDUE);
        if (overdue > 0) {
            recommendations.add(new RecommendationResponse(
                "OVERDUE_INVOICE", "CRITICAL",
                overdue + " invoice(s) are overdue — send payment reminders to clients",
                "/finance/invoices"));
        }

        return recommendations;
    }

  //AI Business Insights
    @Override
    @Transactional(timeout = 30)
    public InsightsResponse getAiInsights() {
        DashboardSummaryResponse summary = getSummary(null, null);

        String metrics = """
            Total leads: %d (new: %d, qualified: %d)
            Accounts: %d
            Open opportunities: %d worth %s (weighted forecast: %s)
            Service requests — pending: %d, in progress: %d, SLA breached (open): %d
            Support tickets — open: %d, new: %d
            Outstanding invoice amount: %s
            Wallet balance: %s (credits: %s)
            """.formatted(
                summary.getTotalLeads(), summary.getNewLeads(), summary.getQualifiedLeads(),
                summary.getTotalClients(),
                summary.getOpenOpportunities(), summary.getPipelineValue(), summary.getWeightedForecast(),
                summary.getPendingRequests(), summary.getInProgressRequests(), summary.getSlaBreachedOpen(),
                summary.getOpenTickets(), summary.getNewTickets(),
                summary.getOutstandingInvoiceAmount(),
                summary.getWalletBalance(), summary.getWalletCreditBalance());

        String prompt = BusinessInsightsPromptBuilder.builder()
            .setMetrics(metrics)
            .build();

        long start = System.currentTimeMillis();
        String insights = aiService.generateFromPrompt(AiFeature.BUSINESS_INSIGHTS, prompt);

        InsightsResponse response = new InsightsResponse();
        response.setInsights(insights);
        response.setGeneratedInMs(System.currentTimeMillis() - start);
        return response;
    }

    // ==================== Platform overview (SaaS staff) ====================

    @Override
    public PlatformSummaryResponse getPlatformSummary() {
        LocalDate today = LocalDate.now();

        List<Role> platformRoles = List.of(
            Role.SUPER_ADMIN, Role.SYSTEM_ADMIN, Role.SUPPORT_AGENT, Role.SUPPORT_MANAGER,
            Role.MARKETING_MANAGER, Role.PLATFORM_ACCOUNTANT, Role.SALES_MANAGER);

        return PlatformSummaryResponse.builder()
            .totalCompanies(companyRepository.count())
            .activeCompanies(companyRepository.countByStatus(CompanyStatus.ACTIVE))
            .trialCompanies(companyRepository.countByStatus(CompanyStatus.TRIAL))
            .suspendedCompanies(companyRepository.countByStatus(CompanyStatus.SUSPENDED))
            .pendingVerificationCompanies(companyRepository.countByStatus(CompanyStatus.PENDING_VERIFICATION))
            .trialsExpiringWithin7Days(companyRepository.countByStatusAndSubscriptionEndBetween(
                CompanyStatus.TRIAL, today, today.plusDays(7)))
            .freePlanCompanies(companyRepository.countBySubscriptionPlan(SubscriptionPlan.FREE))
            .starterPlanCompanies(companyRepository.countBySubscriptionPlan(SubscriptionPlan.STARTER))
            .proPlanCompanies(companyRepository.countBySubscriptionPlan(SubscriptionPlan.PRO))
            .enterprisePlanCompanies(companyRepository.countBySubscriptionPlan(SubscriptionPlan.ENTERPRISE))
            .totalPlatformUsers(userRepository.countByRoleIn(platformRoles))
            .totalRevenue(subscriptionHistoryRepository.sumTotalRevenue())
            .revenueThisMonth(subscriptionHistoryRepository.sumRevenueSince(
                today.withDayOfMonth(1).atStartOfDay()))
            .build();
    }

    // ==================== Client overview (CLIENT users) ====================

    @Override
    public ClientSummaryResponse getClientSummary() {
        User user = securityUtil.getCurrentUser();
        Long companyId = securityUtil.getCurrentCompanyId();
        if (user == null || companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }

        Client client = clientRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException("No client record found for current user"));
        Long clientId = client.getId();

        long pending = serviceRequestRepository.countByCompanyIdAndClientIdAndStatus(
                            companyId, clientId, ServiceRequestStatus.PENDING)
                     + serviceRequestRepository.countByCompanyIdAndClientIdAndStatus(
                            companyId, clientId, ServiceRequestStatus.QUOTATION_PENDING);
        long inProgress = serviceRequestRepository.countByCompanyIdAndClientIdAndStatus(
                            companyId, clientId, ServiceRequestStatus.IN_PROGRESS)
                        + serviceRequestRepository.countByCompanyIdAndClientIdAndStatus(
                            companyId, clientId, ServiceRequestStatus.ASSIGNED);
        long completed = serviceRequestRepository.countByCompanyIdAndClientIdAndStatus(
                            companyId, clientId, ServiceRequestStatus.COMPLETED);

        List<InvoiceStatus> unpaidStatuses =
            List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

        long unpaidInvoices = invoiceRepository.countByCompanyIdAndClientIdAndStatusIn(
            companyId, clientId, unpaidStatuses);
        BigDecimal outstanding = invoiceRepository.sumOutstandingByCompanyIdAndClientId(
            companyId, clientId, unpaidStatuses).orElse(BigDecimal.ZERO);

        return ClientSummaryResponse.builder()
            .pendingRequests(pending)
            .inProgressRequests(inProgress)
            .completedRequests(completed)
            .unpaidInvoices(unpaidInvoices)
            .outstandingInvoiceAmount(outstanding)
            .build();
    }
}
