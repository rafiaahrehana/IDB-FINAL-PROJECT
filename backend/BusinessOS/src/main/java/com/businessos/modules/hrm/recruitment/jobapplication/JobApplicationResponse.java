package com.businessos.modules.hrm.recruitment.jobapplication;

import com.businessos.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JobApplicationResponse {
    private Long id;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String resumeUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private String notes;
    private Long jobPostingId;
    private String jobPostingTitle;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime createdAt;
}
