package com.businessos.modules.crm.opportunity;

import com.businessos.core.automation.AutomationEventPublisher;
import com.businessos.enums.LeadSource;
import com.businessos.enums.NotificationType;
import com.businessos.modules.crm.activity.CrmActivityType;
import com.businessos.modules.crm.activity.CrmActivityService;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.crm.contact.ClientContact;
import com.businessos.modules.crm.contact.ClientContactRepository;
import com.businessos.modules.crm.lead.Lead;
import com.businessos.modules.crm.lead.LeadRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final ClientRepository clientRepository;
    private final ClientContactRepository clientContactRepository;
    private final LeadRepository leadRepository;
    private final EmployeeRepository employeeRepository;
    private final CrmActivityService crmActivityService;
    private final AutomationEventPublisher automationEventPublisher;
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    @Override
    public OpportunityResponse create(OpportunityRequest request) {
        Long companyId = requireCompanyId();
        Client client = clientRepository.findByIdAndCompanyId(request.getClientId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Opportunity opportunity = buildFromRequest(request, client, companyId);
        opportunity = opportunityRepository.save(opportunity);

        crmActivityService.logSystemActivity(CrmActivityType.NOTE,
                "Opportunity created",
                "Opportunity \"" + opportunity.getName() + "\" created in stage " + opportunity.getStage(),
                client.getId(), opportunity.getId());

        return OpportunityMapper.toResponse(opportunity);
    }

    @Override
    public OpportunityResponse createFromLead(Long leadId, OpportunityRequest request) {
        Long companyId = requireCompanyId();
        Lead lead = leadRepository.findByIdAndCompanyId(leadId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found"));

        Client client;
        if (lead.getConvertedClient() != null) {
            client = lead.getConvertedClient();
        } else if (request.getClientId() != null) {
            client = clientRepository.findByIdAndCompanyId(request.getClientId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        } else {
            throw new BadRequestException(
                    "Lead is not converted yet. Convert the lead to a client first or provide a clientId");
        }

        request.setClientId(client.getId());
        Opportunity opportunity = buildFromRequest(request, client, companyId);
        opportunity.setSourceLead(lead);
        if (request.getSource() == null && lead.getSource() != null) {
            opportunity.setSource(lead.getSource());
        }
        if (request.getAmount() == null && lead.getEstimatedValue() != null) {
            opportunity.setAmount(lead.getEstimatedValue());
        }
        if (request.getExpectedCloseDate() == null && lead.getExpectedCloseDate() != null) {
            opportunity.setExpectedCloseDate(lead.getExpectedCloseDate());
        }
        opportunity = opportunityRepository.save(opportunity);

        crmActivityService.logSystemActivity(CrmActivityType.NOTE,
                "Opportunity created from lead",
                "Opportunity \"" + opportunity.getName() + "\" created from lead \"" + lead.getContactName() + "\"",
                client.getId(), opportunity.getId());

        return OpportunityMapper.toResponse(opportunity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OpportunityResponse> listAll(OpportunityStage stage, Long clientId, Long ownerId, String keyword,
            Pageable pageable) {
        Long companyId = requireCompanyId();
        Page<Opportunity> page;
        if (keyword != null && !keyword.isBlank()) {
            page = opportunityRepository.searchOpportunities(companyId, keyword.trim(), pageable);
        } else if (stage != null) {
            page = opportunityRepository.findByCompanyIdAndStage(companyId, stage, pageable);
        } else if (clientId != null) {
            page = opportunityRepository.findByCompanyIdAndClientId(companyId, clientId, pageable);
        } else if (ownerId != null) {
            page = opportunityRepository.findByCompanyIdAndOwnerId(companyId, ownerId, pageable);
        } else {
            page = opportunityRepository.findByCompanyId(companyId, pageable);
        }
        return page.map(OpportunityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OpportunityResponse getById(Long id) {
        return OpportunityMapper.toResponse(findOwned(id));
    }

    @Override
    public OpportunityResponse update(Long id, OpportunityRequest request) {
        Long companyId = requireCompanyId();
        Opportunity opportunity = findOwned(id);

        if (opportunity.getStage().isClosed()) {
            throw new BadRequestException(
                    "Closed opportunities cannot be edited. Reopen it by changing the stage first");
        }

        if (request.getName() != null)
            opportunity.setName(request.getName());
        opportunity.setDescription(request.getDescription());
        opportunity.setNextStep(request.getNextStep());
        if (request.getAmount() != null)
            opportunity.setAmount(request.getAmount());
        if (request.getProbability() != null)
            opportunity.setProbability(request.getProbability());
        if (request.getExpectedCloseDate() != null)
            opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
        if (request.getSource() != null)
            opportunity.setSource(request.getSource());

        if (request.getContactId() != null) {
            ClientContact contact = clientContactRepository.findByIdAndCompanyId(request.getContactId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
            if (!contact.getClient().getId().equals(opportunity.getClient().getId())) {
                throw new BadRequestException("Contact does not belong to the opportunity's client");
            }
            opportunity.setContact(contact);
        }
        if (request.getOwnerId() != null) {
            opportunity.setOwner(findEmployee(request.getOwnerId(), companyId));
        }

        return OpportunityMapper.toResponse(opportunityRepository.save(opportunity));
    }

    @Override
    public OpportunityResponse changeStage(Long id, ChangeStageRequest request) {
        Opportunity opportunity = findOwned(id);
        OpportunityStage from = opportunity.getStage();
        OpportunityStage to = request.getStage();

        if (from == to) {
            return OpportunityMapper.toResponse(opportunity);
        }
        if (to == OpportunityStage.CLOSED_LOST
                && (request.getLostReason() == null || request.getLostReason().isBlank())) {
            throw new BadRequestException("A lost reason is required when closing an opportunity as lost");
        }

        opportunity.setStage(to);
        opportunity.setProbability(to.getDefaultProbability());
        opportunity.setStageChangedAt(LocalDateTime.now());

        if (to.isClosed()) {
            opportunity.setActualCloseDate(LocalDate.now());
            if (to == OpportunityStage.CLOSED_LOST) {
                opportunity.setLostReason(request.getLostReason());
                notifyOwnerOpportunityLost(opportunity);
            } else {
                opportunity.setLostReason(null);
                creditClientLifetimeValue(opportunity);
                automationEventPublisher.publishOpportunityWon(
                        this, opportunity.getCompany().getId(),
                        opportunity.getId(), opportunity.getClient().getId(),
                        opportunity.getName());
            }
        } else {
            opportunity.setActualCloseDate(null);
            opportunity.setLostReason(null);
            // Reopening a previously-won deal must reverse the credit applied when it
            // closed, or a later reopen+reclose double-counts the client's lifetime value.
            if (from == OpportunityStage.CLOSED_WON) {
                debitClientLifetimeValue(opportunity);
            }
        }

        opportunity = opportunityRepository.save(opportunity);

        crmActivityService.logSystemActivity(CrmActivityType.STAGE_CHANGE,
                "Stage changed",
                "Stage moved from " + from + " to " + to,
                opportunity.getClient().getId(), opportunity.getId());

        return OpportunityMapper.toResponse(opportunity);
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineSummaryResponse getPipelineSummary() {
        Long companyId = requireCompanyId();
        List<OpportunityRepository.PipelineStageSummary> rows = opportunityRepository.summarizePipeline(companyId);

        PipelineSummaryResponse response = new PipelineSummaryResponse();
        BigDecimal open = BigDecimal.ZERO;
        BigDecimal weighted = BigDecimal.ZERO;
        BigDecimal won = BigDecimal.ZERO;
        long openDeals = 0;

        EnumSet<OpportunityStage> closed = EnumSet.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST);

        response.setStages(rows.stream().map(row -> {
            PipelineSummaryResponse.StageSummary s = new PipelineSummaryResponse.StageSummary();
            s.setStage(row.getStage());
            s.setDealCount(row.getDealCount());
            s.setTotalAmount(row.getTotalAmount());
            s.setWeightedAmount(row.getWeightedAmount());
            return s;
        }).toList());

        for (OpportunityRepository.PipelineStageSummary row : rows) {
            if (row.getStage() == OpportunityStage.CLOSED_WON) {
                won = won.add(row.getTotalAmount());
            } else if (!closed.contains(row.getStage())) {
                open = open.add(row.getTotalAmount());
                weighted = weighted.add(row.getWeightedAmount());
                openDeals += row.getDealCount();
            }
        }
        response.setOpenPipelineValue(open);
        response.setWeightedForecast(weighted);
        response.setWonValue(won);
        response.setTotalOpenDeals(openDeals);
        return response;
    }

    @Override
    public void delete(Long id) {
        Opportunity opportunity = findOwned(id);
        opportunity.softDelete();
        opportunityRepository.save(opportunity);
    }

    private Opportunity buildFromRequest(OpportunityRequest request, Client client, Long companyId) {
        OpportunityStage stage = request.getStage() != null ? request.getStage() : OpportunityStage.PROSPECTING;

        Opportunity opportunity = Opportunity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .stage(stage)
                .source(request.getSource() != null ? request.getSource() : LeadSource.OTHER)
                .amount(request.getAmount())
                .probability(
                        request.getProbability() != null ? request.getProbability() : stage.getDefaultProbability())
                .expectedCloseDate(request.getExpectedCloseDate())
                .nextStep(request.getNextStep())
                .client(client)
                .company(client.getCompany())
                .build();

        if (request.getContactId() != null) {
            ClientContact contact = clientContactRepository.findByIdAndCompanyId(request.getContactId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
            if (!contact.getClient().getId().equals(client.getId())) {
                throw new BadRequestException("Contact does not belong to the selected client");
            }
            opportunity.setContact(contact);
        }
        if (request.getOwnerId() != null) {
            opportunity.setOwner(findEmployee(request.getOwnerId(), companyId));
        }
        return opportunity;
    }

    private void creditClientLifetimeValue(Opportunity opportunity) {
        if (opportunity.getAmount() == null)
            return;
        Client client = opportunity.getClient();
        BigDecimal current = client.getLifetimeValue() != null ? client.getLifetimeValue() : BigDecimal.ZERO;
        client.setLifetimeValue(current.add(opportunity.getAmount()));
        clientRepository.save(client);
    }

    private void notifyOwnerOpportunityLost(Opportunity opportunity) {
        Employee owner = opportunity.getOwner();
        if (owner == null || owner.getUser() == null) return;
        notificationService.send(CreateNotificationRequest.of(
                NotificationType.GENERAL,
                "Opportunity Lost",
                "Opportunity \"" + opportunity.getName() + "\" was marked as lost.",
                "/crm/pipeline",
                owner.getUser().getId(),
                opportunity.getCompany().getId()
        ));
    }

    /** Reverses creditClientLifetimeValue() when a won opportunity is reopened. */
    private void debitClientLifetimeValue(Opportunity opportunity) {
        if (opportunity.getAmount() == null)
            return;
        Client client = opportunity.getClient();
        BigDecimal current = client.getLifetimeValue() != null ? client.getLifetimeValue() : BigDecimal.ZERO;
        client.setLifetimeValue(current.subtract(opportunity.getAmount()).max(BigDecimal.ZERO));
        clientRepository.save(client);
    }

    private Employee findEmployee(Long employeeId, Long companyId) {
        return employeeRepository.findById(employeeId)
                .filter(e -> e.getCompany() != null && e.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    private Opportunity findOwned(Long id) {
        return opportunityRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found"));
    }

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }
        return companyId;
    }
}
