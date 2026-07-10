package com.businessos.modules.ai.repository;

import com.businessos.modules.ai.entity.AiProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiProviderConfigRepository extends JpaRepository<AiProviderConfig, Long> {

    Optional<AiProviderConfig> findByCompanyIdAndActiveTrue(Long companyId);
}
