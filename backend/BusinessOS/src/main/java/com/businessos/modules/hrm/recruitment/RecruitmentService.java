package com.businessos.modules.hrm.recruitment;


import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationRequest;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecruitmentService {

    JobApplicationResponse apply(Long jobPostingId, JobApplicationRequest request);

    JobApplicationResponse getById(Long id);

    Page<JobApplicationResponse> listByPosting(Long jobPostingId, Pageable pageable);

    Page<JobApplicationResponse> listAll(ApplicationStatus status, Pageable pageable);

    JobApplicationResponse updateStatus(Long id, ApplicationStatus status, String notes);

    void delete(Long id);
}
