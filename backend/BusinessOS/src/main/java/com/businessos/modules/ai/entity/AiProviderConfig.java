package com.businessos.modules.ai.entity;

import com.businessos.modules.ai.enums.AiModel;
import com.businessos.modules.ai.enums.AiProviderType;
import com.businessos.modules.company.Company;
import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "ai_provider_configs",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ai_config_company_provider",
            columnNames = {"company_id", "provider"})
    },
    indexes = {
        @Index(name = "idx_ai_config_company", columnList = "company_id"),
        @Index(name = "idx_ai_config_active",  columnList = "company_id, active")
    }
)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
public class AiProviderConfig extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AiProviderType aiProviderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private AiModel aiModel;

    @Column(name = "api_key_encrypted", length = 512)
    private String apiKeyEncrypted;

    @Column(columnDefinition = "numeric(3,2)")
    private Double temperature;

    private Integer maxTokens;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

}
