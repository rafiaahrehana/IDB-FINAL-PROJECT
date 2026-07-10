package com.businessos.modules.project;

import com.businessos.core.base.BaseEntity;
import com.businessos.modules.company.Company;
import com.businessos.auth.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Project")
@Table(
    name = "projects",
    indexes = {
        @Index(name = "idx_project_company", columnList = "company_id"),
        @Index(name = "idx_project_status", columnList = "company_id, status")
    }
)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private com.businessos.enums.Priority priority = com.businessos.enums.Priority.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "progress", nullable = false)
    @Builder.Default
    private int progress = 0;

    @Column(name = "budget")
    private BigDecimal budget;
}
