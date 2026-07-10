package com.businessos.modules.servicedesk.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequiredDocumentRepository extends JpaRepository<RequiredDocument, Long> {
    List<RequiredDocument> findByCompanyIdAndServiceIdOrderBySortOrderAsc(Long companyId, Long serviceId);
}
