package com.businessos.modules.website;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository("websiteServiceCategoryRepository")
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    List<ServiceCategory> findByCompanyId(Long companyId);
    Optional<ServiceCategory> findBySlugAndCompanyId(String slug, Long companyId);
}
