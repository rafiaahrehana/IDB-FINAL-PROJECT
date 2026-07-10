package com.businessos.modules.hrm.performance;

import com.businessos.modules.hrm.employee.Employee;
import com.businessos.core.base.BaseEntity;
import com.businessos.modules.company.Company;
import com.businessos.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "performance_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id", nullable = false)
    private Employee reviewedBy;

    private LocalDate reviewPeriodStart;
    private LocalDate reviewPeriodEnd;

    // KPI Scores (1-5)
    private Integer scoreWorkQuality;
    private Integer scoreProductivity;
    private Integer scoreCommunication;
    private Integer scoreTeamwork;
    private Integer scoreInitiative;
    private Integer scorePunctuality;
    private Double  overallScore;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String areasForImprovement;

    @Column(columnDefinition = "TEXT")
    private String goalsForNextPeriod;

    @Column(columnDefinition = "TEXT")
    private String managerComments;

    @Column(columnDefinition = "TEXT")
    private String employeeComments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.DRAFT;

    private LocalDate reviewDate;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Builder.Default
    private boolean finalised = false;
}
