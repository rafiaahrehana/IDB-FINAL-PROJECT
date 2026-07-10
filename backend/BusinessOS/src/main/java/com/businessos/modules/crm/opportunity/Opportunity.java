package com.businessos.modules.crm.opportunity;

import com.businessos.core.base.BaseEntity;
import com.businessos.enums.LeadSource;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.contact.ClientContact;
import com.businessos.modules.crm.lead.Lead;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.company.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "opportunities", indexes = {
    @Index(name = "idx_opp_company_stage", columnList = "company_id,stage"),
    @Index(name = "idx_opp_client", columnList = "client_id"),
    @Index(name = "idx_opp_owner", columnList = "owner_id"),
    @Index(name = "idx_opp_close_date", columnList = "expected_close_date"),
    @Index(name = "idx_opp_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Opportunity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OpportunityStage stage = OpportunityStage.PROSPECTING;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private LeadSource source = LeadSource.OTHER;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    // 0 - 100, defaults from stage, can be overridden per deal
    @Column(nullable = false)
    @Builder.Default
    private Integer probability = OpportunityStage.PROSPECTING.getDefaultProbability();

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    private LocalDate actualCloseDate;

    @Column(length = 255)
    private String nextStep;

    @Column(length = 255)
    private String lostReason;

    private LocalDateTime lastActivityAt;
    private LocalDateTime stageChangedAt;

    // The account this deal belongs to
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Primary contact person on the deal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private ClientContact contact;

    // Lead this opportunity originated from (if any)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_lead_id")
    private Lead sourceLead;

    // Sales owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Employee owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @PrePersist
    protected void onCreate() {
        if (this.stage == null) {
            this.stage = OpportunityStage.PROSPECTING;
        }
        if (this.probability == null) {
            this.probability = this.stage.getDefaultProbability();
        }
        if (this.stageChangedAt == null) {
            this.stageChangedAt = LocalDateTime.now();
        }
    }
}
