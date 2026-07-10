package com.businessos.modules.crm.opportunity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OpportunityService {

    OpportunityResponse create(OpportunityRequest request);

    OpportunityResponse createFromLead(Long leadId, OpportunityRequest request);

    Page<OpportunityResponse> listAll(OpportunityStage stage, Long clientId, Long ownerId, String keyword, Pageable pageable);

    OpportunityResponse getById(Long id);

    OpportunityResponse update(Long id, OpportunityRequest request);

    OpportunityResponse changeStage(Long id, ChangeStageRequest request);

    PipelineSummaryResponse getPipelineSummary();

    void delete(Long id);
}
