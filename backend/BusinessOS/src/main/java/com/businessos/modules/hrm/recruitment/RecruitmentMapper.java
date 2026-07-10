package com.businessos.modules.hrm.recruitment;

import com.businessos.auth.user.User;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplication;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationResponse;
import com.businessos.modules.hrm.recruitment.jobpost.JobPosting;

public class RecruitmentMapper {

    public static JobApplicationResponse toJobApplicationResponse(JobApplication a) {
        JobPosting jp = a.getJobPosting();
        User reviewerUser = a.getReviewedBy();
        
        JobApplicationResponse r = new JobApplicationResponse();
        r.setId(a.getId());
        r.setApplicantName(a.getApplicantName());
        r.setApplicantEmail(a.getApplicantEmail());
        r.setApplicantPhone(a.getApplicantPhone());
        r.setResumeUrl(a.getResumeUrl());
        r.setCoverLetter(a.getCoverLetter());
        r.setStatus(a.getStatus());
        r.setNotes(a.getInterviewNotes());
        r.setJobPostingId(jp != null ? jp.getId() : null);
        r.setJobPostingTitle(jp != null ? jp.getTitle() : null);
        r.setReviewedById(reviewerUser != null ? reviewerUser.getId() : null);
        r.setReviewedByName(reviewerUser != null ? reviewerUser.getFullName() : null);
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
