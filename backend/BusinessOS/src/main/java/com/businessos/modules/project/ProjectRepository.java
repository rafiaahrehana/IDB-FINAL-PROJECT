package com.businessos.modules.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByCompanyId(Long companyId, Pageable pageable);

    List<Project> findByCompanyId(Long companyId);

    long countByCompanyId(Long companyId);

    long countByCompanyIdAndStatus(Long companyId, ProjectStatus status);

    @Query("""
        SELECT COALESCE(AVG(p.progress), 0) FROM Project p
        WHERE p.company.id = :companyId AND p.deleted = false
        """)
    double averageProgress(@Param("companyId") Long companyId);
}
