package com.businessos.modules.hrm.recruitment;

import com.businessos.enums.ApplicationStatus;
import com.businessos.enums.JobPostingStatus;
import com.businessos.modules.hrm.employee.CreateEmployeeRequest;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.hrm.employee.EmployeeResponse;
import com.businessos.modules.hrm.employee.EmployeeService;
import com.businessos.modules.hrm.recruitment.jobapplication.HireApplicationRequest;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplication;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationRepository;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationRequest;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationResponse;
import com.businessos.modules.hrm.recruitment.jobpost.JobPosting;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentServiceImpl implements RecruitmentService {

    private final JobApplicationRepository applicationRepository;
    private final com.businessos.modules.hrm.recruitment.jobpost.JobPostingRepository jobPostingRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService           employeeService;
    private final CompanyRepository        companyRepository;
    private final SecurityUtil             securityUtil;
    private final EmailService             emailService;
    private final EmailBranding            emailBranding;
    private final AuthorizationService     authorizationService;

    @Override
    @Transactional
    public JobApplicationResponse apply(Long jobPostingId, JobApplicationRequest request) {
        authorizationService.checkPermission(PermissionCode.APPLICATION_CREATE);
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
        authorizationService.checkPermission(PermissionCode.APPLICATION_VIEW);
        return applicationRepository.findByCompanyIdAndJobPostingId(
                requireCompanyId(), jobPostingId, pageable)
            .map(RecruitmentMapper::toJobApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> listAll(ApplicationStatus status, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.APPLICATION_VIEW);
        Long companyId = requireCompanyId();
        return (status != null
            ? applicationRepository.findByCompanyIdAndStatus(companyId, status, pageable)
            : applicationRepository.findByCompanyId(companyId, pageable))
            .map(RecruitmentMapper::toJobApplicationResponse);
    }

    @Override
    @Transactional
    public JobApplicationResponse updateStatus(Long id, ApplicationStatus status, String notes) {
        authorizationService.checkPermission(PermissionCode.APPLICATION_UPDATE);
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
                // Best-effort notification — a failed email must not roll back the status change.
                log.warn("Offer letter email failed (application status still updated): {}", ex.getMessage());
            }
        }

        return RecruitmentMapper.toJobApplicationResponse(application);
    }

    @Override
    @Transactional
    public EmployeeResponse hire(Long id, HireApplicationRequest request) {
        authorizationService.checkPermission(PermissionCode.APPLICATION_UPDATE);
        JobApplication application = findInTenant(id);

        if (application.getConvertedEmployee() != null) {
            throw new BadRequestException("This application has already been converted to an employee");
        }
        if (application.getStatus() != ApplicationStatus.OFFERED) {
            throw new BadRequestException(
                "Only candidates with status OFFERED can be hired. Current status: " + application.getStatus());
        }

        JobPosting posting = application.getJobPosting();
        String[] name = splitApplicantName(application.getApplicantName());

        CreateEmployeeRequest createRequest = new CreateEmployeeRequest();
        createRequest.setFirstName(name[0]);
        createRequest.setLastName(name[1]);
        createRequest.setEmail(application.getApplicantEmail());
        createRequest.setPassword(request.getPassword());
        createRequest.setOfficialEmail(request.getOfficialEmail());
        createRequest.setWorkPhone(application.getApplicantPhone());
        createRequest.setJobTitle(posting != null ? posting.getTitle() : null);
        createRequest.setEmploymentType(request.getEmploymentType() != null
            ? request.getEmploymentType()
            : (posting != null ? posting.getEmploymentType() : null));
        createRequest.setDepartmentId(request.getDepartmentId() != null
            ? request.getDepartmentId()
            : (posting != null && posting.getDepartment() != null ? posting.getDepartment().getId() : null));
        createRequest.setDesignationId(request.getDesignationId());
        createRequest.setReportingManagerId(request.getReportingManagerId());
        createRequest.setShiftId(request.getShiftId());
        createRequest.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        createRequest.setConfirmationDate(request.getConfirmationDate());
        createRequest.setProbationEndDate(request.getProbationEndDate());
        createRequest.setContractEndDate(request.getContractEndDate());
        createRequest.setBasicSalary(request.getBasicSalary());
        createRequest.setHouseRent(request.getHouseRent());
        createRequest.setMedicalAllowance(request.getMedicalAllowance());
        createRequest.setTransportAllowance(request.getTransportAllowance());
        createRequest.setBankName(request.getBankName());
        createRequest.setBankAccountNumber(request.getBankAccountNumber());
        createRequest.setEmergencyContactName(request.getEmergencyContactName());
        createRequest.setEmergencyContactPhone(request.getEmergencyContactPhone());
        createRequest.setEmergencyContactRelation(request.getEmergencyContactRelation());

        // Delegates to the same onboarding path as a manual hire: portal user creation,
        // notification defaults, and the welcome email all happen inside employeeService.create().
        EmployeeResponse employeeResponse = employeeService.create(createRequest);

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeResponse.getId(), requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeResponse.getId()));

        application.setStatus(ApplicationStatus.HIRED);
        application.setConvertedEmployee(employee);
        application.setConvertedAt(LocalDateTime.now());
        employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
            .ifPresent(reviewer -> application.setReviewedBy(reviewer.getUser()));

        return employeeResponse;
    }

    /** Applicants only supply one free-text name field; Employee onboarding needs first/last separately. */
    private String[] splitApplicantName(String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        int idx = trimmed.indexOf(' ');
        if (idx < 0) {
            return new String[] { trimmed, trimmed };
        }
        String first = trimmed.substring(0, idx).trim();
        String last = trimmed.substring(idx + 1).trim();
        return new String[] { first, last.isEmpty() ? first : last };
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.APPLICATION_DELETE);
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
