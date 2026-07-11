package com.businessos.modules.hrm.recruitment.jobapplication;

import com.businessos.modules.hrm.recruitment.jobpost.JobPosting;
import com.businessos.core.base.BaseEntity;
import com.businessos.auth.user.User;
import com.businessos.modules.company.Company;
import com.businessos.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "job_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false) private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) private Company company;

    // Applicant info (external, not a platformuser yet)
    @Column(nullable = false) private String applicantName;
    @Column(nullable = false) private String applicantEmail;
    private String applicantPhone;
    private String resumeUrl;
    private String coverLetter;
    private String linkedInUrl;
    private String portfolioUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private ApplicationStatus status = ApplicationStatus.APPLIED;

    private LocalDateTime interviewAt;
    private String interviewNotes;
    @Column(columnDefinition = "TEXT") private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id") private User reviewedBy;
}
