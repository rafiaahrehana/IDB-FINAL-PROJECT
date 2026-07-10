package com.businessos.modules.hrm.recruitment.jobapplication;

import com.businessos.modules.hrm.recruitment.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByIdAndCompanyId(Long id, Long companyId);

    Page<JobApplication> findByCompanyId(Long companyId, Pageable pageable);

    Page<JobApplication> findByCompanyIdAndJobPostingId(
        Long companyId, Long jobPostingId, Pageable pageable);

    Page<JobApplication> findByCompanyIdAndStatus(
        Long companyId, ApplicationStatus status, Pageable pageable);

    boolean existsByJobPostingIdAndApplicantEmail(Long jobPostingId, String email);

    long countByCompanyIdAndJobPostingId(Long companyId, Long jobPostingId);
}
