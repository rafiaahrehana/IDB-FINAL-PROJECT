package com.businessos.modules.website;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {
    Optional<CmsPage> findBySlugAndCompanyId(String slug, Long companyId);
}
