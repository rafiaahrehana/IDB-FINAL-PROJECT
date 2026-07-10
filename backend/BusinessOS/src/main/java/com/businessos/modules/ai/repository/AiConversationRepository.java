package com.businessos.modules.ai.repository;


import com.businessos.modules.ai.entity.AiConversation;
import com.businessos.modules.ai.enums.AiFeature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    Page<AiConversation> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    Page<AiConversation> findByCompanyIdAndFeatureOrderByCreatedAtDesc(
            Long companyId, AiFeature feature, Pageable pageable);
}
