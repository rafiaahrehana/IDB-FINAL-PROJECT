package com.businessos.modules.hrm.recruitment;

import com.businessos.enums.ApplicationStatus;
import com.businessos.enums.JobPostingStatus;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplication;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationRepository;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationRequest;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationResponse;
import com.businessos.modules.hrm.recruitment.jobpost.JobPosting;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class RecruitmentServiceImpl implements RecruitmentService {

    private final JobApplicationRepository applicationRepository;
    private final com.businessos.modules.hrm.recruitment.jobpost.JobPostingRepository jobPostingRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository        companyRepository;
    private final SecurityUtil             securityUtil;
    private final EmailService             emailService;
    private final EmailBranding            emailBranding;

    @Override
    @Transactional
    public JobApplicationResponse apply(Long jobPostingId, JobApplicationRequest request) {
        Long companyId = requireCompanyId();
        JobPosting posting = jobPostingRepository.findByIdAndCompanyId(jobPostingId, companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Job posting not found: " + jobPostingId));

        if (posting.getStatus() != JobPostingStatus.OPEN) {
            throw new BadRequestException("This position is no longer accepting applications");
        }
        if (applicationRepository.existsByJobPostingIdAndApplicantEmail(
                jobPostingId, request.getApplicantEmail().toLowerCase().trim())) {
            throw new BadRequestException("An application from this email already exists for this position");
        }

        JobApplication application = JobApplication.builder()
            .jobPosting(posting)
            .company(companyRef(companyId))
            .applicantName(request.getApplicantName())
            .applicantEmail(request.getApplicantEmail().toLowerCase().trim())
            .applicantPhone(request.getApplicantPhone())
            .resumeUrl(request.getResumeUrl())
            .coverLetter(request.getCoverLetter())
            .status(ApplicationStatus.APPLIED)
            .build();

        applicationRepository.save(application);
        
        return RecruitmentMapper.toJobApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getById(Long id) {
        return RecruitmentMapper.toJobApplicationResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> listByPosting(Long jobPostingId, Pageable pageable) {
        return applicationRepository.findByCompanyIdAndJobPostingId(
                requireCompanyId(), jobPostingId, pageable)
            .map(RecruitmentMapper::toJobApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> listAll(ApplicationStatus status, Pageable pageable) {
        Long companyId = requireCompanyId();
        return (status != null
            ? applicationRepository.findByCompanyIdAndStatus(companyId, status, pageable)
            : applicationRepository.findByCompanyId(companyId, pageable))
            .map(RecruitmentMapper::toJobApplicationResponse);
    }

    @Override
    @Transactional
    public JobApplicationResponse updateStatus(Long id, ApplicationStatus status, String notes) {
        Long companyId = requireCompanyId();
        JobApplication application = findInTenant(id);
        Employee reviewer = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
            .orElseThrow(() -> new BadRequestException("Employee profile not found"));
        application.setStatus(status);
        if (notes != null) application.setInterviewNotes(notes);
        application.setReviewedBy(reviewer.getUser());

        if (status == ApplicationStatus.OFFERED) {
            try {
                Company fullCompany = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                EmailBranding.Data branding = emailBranding.from(fullCompany);
                emailService.sendOfferLetterEmail(application.getApplicantEmail(), application.getApplicantName(), branding);
                
            } catch (Exception ex) {
                throw new com.businessos.shared.exception.BadRequestException("Internal error during operation: " + ex.getMessage());
            }
        }

        return RecruitmentMapper.toJobApplicationResponse(application);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findInTenant(id).softDelete();
    }

    private JobApplication findInTenant(Long id) {
        return applicationRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company(); c.setId(companyId); return c;
    }
}
