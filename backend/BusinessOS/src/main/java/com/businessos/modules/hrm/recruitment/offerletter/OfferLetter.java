package com.businessos.modules.hrm.recruitment.offerletter;

import com.businessos.modules.hrm.employee.Employee;
import com.businessos.core.base.BaseEntity;
import com.businessos.auth.user.User;
import com.businessos.modules.company.Company;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "employment_letters", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "reference_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OfferLetter extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LetterType letterType;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String signedBy;

    private LocalDate issueDate;

    private String fileUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private boolean issued = false;
    @Builder.Default
    private boolean acknowledged = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
