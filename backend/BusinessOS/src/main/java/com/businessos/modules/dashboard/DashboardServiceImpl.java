package com.businessos.modules.dashboard;

import com.businessos.enums.InvoiceStatus;
import com.businessos.enums.LeadStatus;
import com.businessos.enums.ServiceRequestStatus;
import com.businessos.enums.TaskStatus;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.crm.lead.LeadRepository;
import com.businessos.modules.crm.opportunity.Opportunity;
import com.businessos.modules.crm.opportunity.OpportunityRepository;
import com.businessos.modules.crm.opportunity.OpportunityStage;
import com.businessos.modules.finance.invoice.ClientInvoice;
import com.businessos.modules.finance.invoice.ClientInvoiceRepository;
import com.businessos.modules.hrm.announcement.AnnouncementRepository;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.service.AiService;
import com.businessos.modules.itam.software.SoftwareLicense;
import com.businessos.modules.itam.software.SoftwareLicenseRepository;
import com.businessos.modules.project.ProjectService;
import com.businessos.modules.project.meeting.MeetingService;
import com.businessos.modules.project.task.TaskService;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequestRepository;
import com.businessos.modules.support.ticket.SupportTicketRepository;
import com.businessos.modules.support.ticket.TicketStatus;
import com.businessos.shared.payment.wallet.Wallet;
import com.businessos.shared.payment.wallet.WalletRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepository leadRepository;
    private final ClientRepository clientRepository;
    private final OpportunityRepository opportunityRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ClientInvoiceRepository invoiceRepository;
    private final WalletRepository walletRepository;
    private final SecurityUtil securityUtil;
    private final EmployeeRepository employeeRepository;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final MeetingService meetingService;
    private final AnnouncementRepository announcementRepository;
    private final SoftwareLicenseRepository licenseRepository;
    private final AiService aiService;

    @Override
    public DashboardSummaryResponse getSummary() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }

        // CRM
        long totalLeads = leadRepository.countByCompanyId(companyId);
        long newLeads = leadRepository.countByCompanyIdAndStatus(companyId, LeadStatus.NEW);
        long qualifiedLeads = leadRepository.countByCompanyIdAndStatus(companyId, LeadStatus.QUALIFIED);
        long totalClients = clientRepository.countByCompanyId(companyId);

        // Pipeline summary (reuses the existing projection query)
        var pipelineStages = opportunityRepository.summarizePipeline(companyId);
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
        long pendingRequests = serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.PENDING)
                + serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.QUOTATION_PENDING);
        long inProgressRequests = serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.IN_PROGRESS)
                + serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.ASSIGNED);
        long completedAllTime = serviceRequestRepository.countByCompanyIdAndStatus(companyId, ServiceRequestStatus.COMPLETED);
        long slaBreached = serviceRequestRepository.countByCompanyIdAndSlaBreachTrueAndStatusNotIn(
                companyId, List.of(ServiceRequestStatus.COMPLETED,
                        ServiceRequestStatus.CANCELLED,
                        ServiceRequestStatus.REJECTED));

        // Support tickets
        long openTickets = supportTicketRepository.countByStatusAndCompanyId(TicketStatus.OPEN, companyId)
                + supportTicketRepository.countByStatusAndCompanyId(TicketStatus.IN_PROGRESS, companyId)
                + supportTicketRepository.countByStatusAndCompanyId(TicketStatus.WAITING, companyId);
        long newTickets = supportTicketRepository.countByStatusAndCompanyId(TicketStatus.NEW, companyId);

        // Finance
        BigDecimal outstanding = invoiceRepository.sumOutstandingByCompanyId(
                        companyId,
                        List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE))
                .orElse(BigDecimal.ZERO);

        Wallet wallet = walletRepository.findByContextTypeAndContextId("COMPANY", companyId).orElse(null);
        BigDecimal walletBalance = wallet != null ? wallet.getBalance() : BigDecimal.ZERO;
        BigDecimal walletCreditBalance = wallet != null ? wallet.getCreditBalance() : BigDecimal.ZERO;

        // HRM / Projects / Tasks / Meetings
        long totalEmployees = employeeRepository.countByCompanyId(companyId);
        long activeProjects = projectService.getActiveCount();
        long openTasks = taskService.getOpenCount();
        long meetingsToday = meetingService.getTodayCount();
        double projectProgress = projectService.getAverageProgress();

        long taskPending = taskService.countByStatus(TaskStatus.PENDING);
        long taskInProgress = taskService.countByStatus(TaskStatus.IN_PROGRESS);
        long taskCompleted = taskService.countByStatus(TaskStatus.COMPLETED);
        long taskBlocked = taskService.countByStatus(TaskStatus.BLOCKED);
        long taskCancelled = taskService.countByStatus(TaskStatus.CANCELLED);

        List<com.businessos.modules.hrm.announcement.AnnouncementResponse> announcements =
                announcementRepository.findActiveByCompanyId(companyId, java.time.LocalDateTime.now())
                        .stream().map(this::toAnnouncementDTO).toList();

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
                .totalEmployees(totalEmployees)
                .activeProjects(activeProjects)
                .openTasks(openTasks)
                .meetingsToday(meetingsToday)
                .projectProgress(projectProgress)
                .taskPending(taskPending)
                .taskInProgress(taskInProgress)
                .taskCompleted(taskCompleted)
                .taskBlocked(taskBlocked)
                .taskCancelled(taskCancelled)
                .announcements(announcements)
                .build();
    }

    private com.businessos.modules.hrm.announcement.AnnouncementResponse toAnnouncementDTO(
            com.businessos.modules.hrm.announcement.Announcement a) {
        com.businessos.modules.hrm.announcement.AnnouncementResponse r =
                new com.businessos.modules.hrm.announcement.AnnouncementResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setBody(a.getBody());
        r.setAudience(a.getAudience());
        r.setPublished(a.isPublished());
        r.setPriority(a.getPriority());
        if (a.getCreatedBy() != null) {
            r.setCreatedById(a.getCreatedBy().getId());
            r.setCreatedByName(a.getCreatedBy().getFullName());
        }
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }

    @Override
    public List<RecommendationResponse> getRecommendations() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context found.");
        }

        List<RecommendationResponse> recs = new ArrayList<>();

        // Stale deals: open opportunities with no activity in over 30 days
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<OpportunityStage> closedStages = List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST);
        List<Opportunity> staleDeals = opportunityRepository
                .findStaleOpenOpportunities(companyId, closedStages, cutoff, Pageable.unpaged());
        if (!staleDeals.isEmpty()) {
            recs.add(new RecommendationResponse("FOLLOW_UP", "WARNING",
                    staleDeals.size() + " open deals have had no activity in over 30 days. Follow up to keep them moving.",
                    "/crm/opportunities"));
        }

        // SLA risk: service requests that breached SLA and are still open
        long slaBreached = serviceRequestRepository.countByCompanyIdAndSlaBreachTrueAndStatusNotIn(
                companyId, List.of(ServiceRequestStatus.COMPLETED,
                        ServiceRequestStatus.CANCELLED,
                        ServiceRequestStatus.REJECTED));
        if (slaBreached > 0) {
            recs.add(new RecommendationResponse("SLA_RISK", "CRITICAL",
                    slaBreached + " service requests have breached their SLA. Review and assign them.",
                    "/servicedesk/requests"));
        }

        // License expiry: software licenses expiring within the next 30 days
        List<SoftwareLicense> expiringLicenses = licenseRepository.findExpiringBetweenDates(
                companyId, LocalDate.now(), LocalDate.now().plusDays(30));
        if (!expiringLicenses.isEmpty()) {
            recs.add(new RecommendationResponse("LICENSE_EXPIRY", "WARNING",
                    expiringLicenses.size() + " software licenses expire within the next 30 days. Plan renewals.",
                    "/itam/licenses"));
        }

        // Overdue invoices
        List<ClientInvoice> overdueInvoices = invoiceRepository.findOverdueInvoices(
                companyId, List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED));
        if (!overdueInvoices.isEmpty()) {
            recs.add(new RecommendationResponse("OVERDUE_INVOICE", "CRITICAL",
                    overdueInvoices.size() + " client invoices are overdue. Send reminders to improve cash flow.",
                    "/finance/invoices"));
        }

        return recs;
    }

    @Override
    public InsightsResponse getAiInsights() {
        DashboardSummaryResponse summary = getSummary();
        String prompt = buildInsightsPrompt(summary);
        long start = System.currentTimeMillis();
        String insights = aiService.generateFromPrompt(AiFeature.BUSINESS_INSIGHTS, prompt);
        long generatedInMs = System.currentTimeMillis() - start;

        InsightsResponse response = new InsightsResponse();
        response.setInsights(insights);
        response.setGeneratedInMs(generatedInMs);
        return response;
    }

    private String buildInsightsPrompt(DashboardSummaryResponse s) {
        return """
                You are a business analyst. Based on the company dashboard summary below,
                provide 3-5 short, actionable insights in bullet points.

                Company snapshot:
                - Total clients: %d
                - Open opportunities: %d
                - Open service requests: %d (SLA breached: %d)
                - Open support tickets: %d
                - Outstanding invoice amount: %s
                - Employees: %d
                - Active projects: %d
                - Open tasks: %d
                - Meetings today: %d
                """.formatted(
                s.getTotalClients(),
                s.getOpenOpportunities(),
                s.getPendingRequests() + s.getInProgressRequests(),
                s.getSlaBreachedOpen(),
                s.getOpenTickets(),
                s.getOutstandingInvoiceAmount(),
                s.getTotalEmployees(),
                s.getActiveProjects(),
                s.getOpenTasks(),
                s.getMeetingsToday());
    }
}
