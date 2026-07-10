package com.businessos.modules.website;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByCompanyIdOrderByPublishedAtDesc(Long companyId);
    Optional<BlogPost> findBySlugAndCompanyId(String slug, Long companyId);
    List<BlogPost> findByCompanyIdAndCategoryIgnoreCase(String category, Long companyId);
}
