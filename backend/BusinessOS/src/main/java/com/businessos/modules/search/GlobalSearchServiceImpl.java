package com.businessos.modules.search;

import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.prompt.SearchAnswerPromptBuilder;
import com.businessos.modules.ai.service.AiService;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.crm.lead.LeadRepository;
import com.businessos.modules.crm.opportunity.OpportunityRepository;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequestRepository;
import com.businessos.modules.support.ticket.SupportTicketRepository;
import com.businessos.modules.finance.invoice.ClientInvoiceRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private static final int PER_TYPE_LIMIT = 5;

    private final LeadRepository leadRepository;
    private final ClientRepository clientRepository;
    private final OpportunityRepository opportunityRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ClientInvoiceRepository invoiceRepository;
    private final AiService aiService;
    private final SecurityUtil securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    public GlobalSearchResponse search(String query) {
        Long companyId = requireCompanyId();
        if (query == null || query.trim().length() < 2) {
            throw new BadRequestException("Search query must be at least 2 characters");
        }
        String keyword = query.trim();
        Pageable top = PageRequest.of(0, PER_TYPE_LIMIT);

        GlobalSearchResponse response = new GlobalSearchResponse();
        response.setQuery(keyword);
        List<SearchResultItem> results = response.getResults();

        // CRM — leads, clients, opportunities (require CLIENT_VIEW)
        if (authorizationService.hasPermission(PermissionCode.CLIENT_VIEW)) {
            leadRepository.searchLeads(companyId, keyword, top)
                    .forEach(lead -> results.add(new SearchResultItem("LEAD", lead.getId(),
                            lead.getContactName(),
                            (lead.getCompanyName() != null ? lead.getCompanyName() + " · " : "") + lead.getStatus(),
                            "/crm/leads")));

            clientRepository.searchClients(companyId, keyword, top)
                    .forEach(client -> results.add(new SearchResultItem("CLIENT", client.getId(),
                            client.getClientCompanyName() != null ? client.getClientCompanyName()
                                    : client.getUser().getFirstName() + " " + client.getUser().getLastName(),
                            client.getIndustry() != null ? client.getIndustry() : "Account",
                            "/crm/clients/" + client.getId())));

            opportunityRepository.searchOpportunities(companyId, keyword, top)
                    .forEach(opp -> results.add(new SearchResultItem("OPPORTUNITY", opp.getId(),
                            opp.getName(),
                            opp.getStage() + (opp.getAmount() != null ? " · " + opp.getAmount() : ""),
                            "/crm/pipeline")));
        }

        // Service desk — service requests (require SERVICE_REQUEST_VIEW)
        if (authorizationService.hasPermission(PermissionCode.SERVICE_REQUEST_VIEW)) {
            serviceRequestRepository.findByCompanyIdAndTitleContainingIgnoreCaseAndDeletedFalse(companyId, keyword, top)
                    .forEach(sr -> results.add(new SearchResultItem("SERVICE_REQUEST", sr.getId(),
                            sr.getTitle(), String.valueOf(sr.getStatus()),
                            "/servicedesk/requests/" + sr.getId())));

            supportTicketRepository.findByCompanyIdAndTitleContainingIgnoreCase(companyId, keyword, top)
                    .forEach(ticket -> results.add(new SearchResultItem("TICKET", ticket.getId(),
                            ticket.getTitle(),
                            ticket.getTicketNumber() + " · " + ticket.getStatus(),
                            "/support/tickets")));
        }

        // Finance — invoices (require EXPENSE_VIEW)
        if (authorizationService.hasPermission(PermissionCode.EXPENSE_VIEW)) {
            invoiceRepository.findByCompanyIdAndInvoiceNumberContainingIgnoreCase(companyId, keyword, top)
                    .forEach(invoice -> results.add(new SearchResultItem("INVOICE", invoice.getId(),
                            invoice.getInvoiceNumber(),
                            invoice.getStatus() + " · " + invoice.getTotalAmount(),
                            "/finance/invoices")));
        }

        response.setTotalMatches(results.size());
        return response;
    }

    @Override
    public AskResponse ask(AskRequest request) {
        GlobalSearchResponse searchResults = search(request.getQuestion());

        StringBuilder context = new StringBuilder();
        for (SearchResultItem item : searchResults.getResults()) {
            context.append("- [").append(item.getType()).append("] ")
                    .append(item.getTitle()).append(" (").append(item.getSubtitle()).append(")\n");
        }

        String prompt = SearchAnswerPromptBuilder.builder()
                .setQuestion(request.getQuestion())
                .setContext(context.isEmpty() ? null : context.toString())
                .build();

        String answer = aiService.generateFromPrompt(AiFeature.SEARCH_ANSWER, prompt);

        AskResponse response = new AskResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswer(answer);
        response.setSources(searchResults.getResults());
        return response;
    }

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }
        return companyId;
    }
}
