package com.businessos.modules.hrm.recruitment.jobpost;

import com.businessos.enums.JobPostingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobPostingService {

    JobPostingResponse create(JobPostingRequest request);

    JobPostingResponse getById(Long id);

    Page<JobPostingResponse> listAll(JobPostingStatus status, Pageable pageable);

    /** Lightweight, ungated - the open-posting picker used by the Applications page. */
    List<JobPostingResponse> listOpen();

    JobPostingResponse update(Long id, JobPostingRequest request);

    JobPostingResponse publish(Long id);

    JobPostingResponse close(Long id);

    void delete(Long id);

}
