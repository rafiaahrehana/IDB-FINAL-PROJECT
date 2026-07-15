package com.businessos.modules.crm.activity;

import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.crm.opportunity.Opportunity;
import com.businessos.modules.crm.opportunity.OpportunityRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CrmActivityServiceImpl implements CrmActivityService {

    private final CrmActivityRepository crmActivityRepository;
    private final ClientRepository clientRepository;
    private final OpportunityRepository opportunityRepository;
    private final SecurityUtil securityUtil;

    @Override
    public CrmActivityResponse log(CrmActivityRequest request) {
        Long companyId = requireCompanyId();
        if (request.getClientId() == null && request.getOpportunityId() == null) {
            throw new BadRequestException("An activity must reference a client or an opportunity");
        }

        CrmActivity.CrmActivityBuilder builder = CrmActivity.builder()
            .type(request.getType())
            .subject(request.getSubject())
            .description(request.getDescription())
            .activityDate(request.getActivityDate() != null ? request.getActivityDate() : LocalDateTime.now())
            .scheduledAt(request.getScheduledAt())
            .completed(request.getCompleted() == null || request.getCompleted())
            .systemGenerated(false)
            .performedBy(securityUtil.getCurrentUser());

        Client client = null;
        Opportunity opportunity = null;

        if (request.getOpportunityId() != null) {
            opportunity = opportunityRepository.findByIdAndCompanyId(request.getOpportunityId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found"));
            client = opportunity.getClient();
            opportunity.setLastActivityAt(LocalDateTime.now());
        }
        if (request.getClientId() != null) {
            client = clientRepository.findByIdAndCompanyId(request.getClientId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            if (opportunity != null && !client.getId().equals(opportunity.getClient().getId())) {
                throw new BadRequestException("Client does not belong to the referenced opportunity");
            }
        }

        CrmActivity activity = builder
            .client(client)
            .opportunity(opportunity)
            .company(client != null ? client.getCompany() : opportunity.getCompany())
            .build();

        return CrmActivityMapper.toResponse(crmActivityRepository.save(activity));
    }

    @Override
    public void logSystemActivity(CrmActivityType type, String subject, String description,
                                  Long clientId, Long opportunityId) {
        Long companyId = requireCompanyId();

        Client client = clientId != null
            ? clientRepository.findByIdAndCompanyId(clientId, companyId).orElse(null)
            : null;
        Opportunity opportunity = opportunityId != null
            ? opportunityRepository.findByIdAndCompanyId(opportunityId, companyId).orElse(null)
            : null;

        if (client == null && opportunity == null) {
            return; // nothing to attach the timeline entry to
        }

        CrmActivity activity = CrmActivity.builder()
            .type(type)
            .subject(subject)
            .description(description)
            .activityDate(LocalDateTime.now())
            .completed(true)
            .systemGenerated(true)
            .performedBy(securityUtil.getCurrentUser())
            .client(client)
            .opportunity(opportunity)
            .company(client != null ? client.getCompany() : opportunity.getCompany())
            .build();

        crmActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CrmActivityResponse> getTimeline(Long clientId, Long opportunityId, Pageable pageable) {
        Long companyId = requireCompanyId();
        Page<CrmActivity> page;
        if (opportunityId != null) {
            page = crmActivityRepository.findByCompanyIdAndOpportunityIdOrderByActivityDateDesc(companyId, opportunityId, pageable);
        } else if (clientId != null) {
            page = crmActivityRepository.findByCompanyIdAndClientIdOrderByActivityDateDesc(companyId, clientId, pageable);
        } else {
            page = crmActivityRepository.findByCompanyIdOrderByActivityDateDesc(companyId, pageable);
        }
        return page.map(CrmActivityMapper::toResponse);
    }

    @Override
    public CrmActivityResponse markCompleted(Long id) {
        CrmActivity activity = findOwned(id);
        activity.setCompleted(true);
        return CrmActivityMapper.toResponse(crmActivityRepository.save(activity));
    }

    @Override
    public void delete(Long id) {
        CrmActivity activity = findOwned(id);
        if (activity.isSystemGenerated()) {
            throw new BadRequestException("System-generated timeline entries cannot be deleted");
        }
        activity.softDelete();
        crmActivityRepository.save(activity);
    }

    private CrmActivity findOwned(Long id) {
        return crmActivityRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null) {
            throw new BadRequestException("No company context for current platformuser");
        }
        return companyId;
    }
}
