package com.businessos.modules.crm.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmActivityRepository extends JpaRepository<CrmActivity, Long> {

    Optional<CrmActivity> findByIdAndCompanyId(Long id, Long companyId);

    Page<CrmActivity> findByCompanyIdAndClientIdOrderByActivityDateDesc(Long companyId, Long clientId, Pageable pageable);

    Page<CrmActivity> findByCompanyIdAndOpportunityIdOrderByActivityDateDesc(Long companyId, Long opportunityId, Pageable pageable);

    Page<CrmActivity> findByCompanyIdOrderByActivityDateDesc(Long companyId, Pageable pageable);

    Page<CrmActivity> findByLeadId(Long leadId, Pageable pageable);
}
