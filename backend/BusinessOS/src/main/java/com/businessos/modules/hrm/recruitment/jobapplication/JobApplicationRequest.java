package com.businessos.modules.hrm.recruitment.jobapplication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobApplicationRequest {
    @NotBlank(message = "Applicant name is required")
    @Size(max = 150)
    private String applicantName;
    @NotBlank(message = "Email is required")
    @Email
    private String applicantEmail;
    @Size(max = 30)
    private String applicantPhone;
    @Size(max = 500)
    private String resumeUrl;
    private String coverLetter;
}
