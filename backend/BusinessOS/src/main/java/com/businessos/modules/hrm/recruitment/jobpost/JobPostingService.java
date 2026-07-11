package com.businessos.modules.hrm.recruitment.jobpost;

import com.businessos.enums.JobPostingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobPostingService {

    JobPostingResponse create(JobPostingRequest request);

    JobPostingResponse getById(Long id);

    Page<JobPostingResponse> listAll(JobPostingStatus status, Pageable pageable);

    JobPostingResponse update(Long id, JobPostingRequest request);

    JobPostingResponse publish(Long id);

    JobPostingResponse close(Long id);

    void delete(Long id);

}
