package com.businessos.modules.servicedesk.kb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {

    // clientOnly=true forces PUBLISHED + clientVisible regardless of the requested status,
    // so a client can never see drafts/archived articles by passing a different status filter.
    @Query("""
        SELECT a FROM KbArticle a
        WHERE (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(a.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (
            (:clientOnly = true AND a.status = com.businessos.modules.servicedesk.kb.KbArticleStatus.PUBLISHED AND a.clientVisible = true)
            OR
            (:clientOnly = false AND (:status IS NULL OR a.status = :status))
        )
        """)
    Page<KbArticle> search(
        @Param("keyword") String keyword,
        @Param("status") KbArticleStatus status,
        @Param("clientOnly") boolean clientOnly,
        Pageable pageable
    );
}
