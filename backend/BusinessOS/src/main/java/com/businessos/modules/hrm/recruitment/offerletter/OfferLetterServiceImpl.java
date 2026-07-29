package com.businessos.modules.hrm.recruitment.offerletter;

import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplication;
import com.businessos.modules.hrm.recruitment.jobapplication.JobApplicationRepository;
import com.businessos.enums.LetterType;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.prompt.EmploymentLetterPromptBuilder;
import com.businessos.modules.ai.service.AiService;

@Service
@RequiredArgsConstructor

public class OfferLetterServiceImpl implements OfferLetterService {

    private final OfferLetterRepository      letterRepository;
    private final EmployeeRepository         employeeRepository;
    private final JobApplicationRepository   jobApplicationRepository;
    private final CompanyRepository          companyRepository;
    private final SecurityUtil               securityUtil;
    private final AiService                  aiService;
    private final AuthorizationService       authorizationService;

    /** OFFER and APPOINTMENT letters go to a recruitment candidate, not an employee. */
    private boolean isPreEmploymentLetter(LetterType type) {
        return type == LetterType.OFFER || type == LetterType.APPOINTMENT;
    }

    @Override
    @Transactional
    public OfferLetterResponse create(OfferLetterRequest request) {
        authorizationService.checkPermission(PermissionCode.LETTER_CREATE);
        Long companyId = requireCompanyId();
        boolean candidateLetter = isPreEmploymentLetter(request.getLetterType());

        Employee employee = null;
        JobApplication application = null;
        String recipientName;
        String recipientEmail;

        if (candidateLetter) {
            // OFFER / APPOINTMENT — recipient is a recruitment candidate who hasn't joined yet.
            if (request.getJobApplicationId() == null) {
                throw new BadRequestException(
                    request.getLetterType() + " letters must be addressed to a recruitment candidate");
            }
            application = jobApplicationRepository.findByIdAndCompanyId(request.getJobApplicationId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Candidate not found: " + request.getJobApplicationId()));
            recipientName = application.getApplicantName();
            recipientEmail = application.getApplicantEmail();
        } else {
            // All other letters — recipient is an existing employee.
            if (request.getEmployeeId() == null) {
                throw new BadRequestException(
                    request.getLetterType() + " letters must be addressed to an employee");
            }
            employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found: " + request.getEmployeeId()));
            recipientName = employee.getUser() != null ? employee.getUser().getFullName() : null;
            recipientEmail = employee.getUser() != null ? employee.getUser().getEmail() : null;
        }

        if (request.getReferenceNumber() != null
                && letterRepository.existsByCompanyIdAndReferenceNumber(companyId, request.getReferenceNumber())) {
            throw new BadRequestException("Reference number already exists: " + request.getReferenceNumber());
        }

        String content = request.getContent();
        if (content == null || content.isBlank()) {
            content = aiService.generateRaw(AiFeature.EMPLOYMENT_LETTER, candidateLetter
                ? buildCandidatePrompt(companyId, application, request.getLetterType().name())
                : buildLetterPrompt(companyId, employee, request.getLetterType().name()));
        }

        OfferLetter letter = OfferLetter.builder()
            .employee(employee)
            .jobApplication(application)
            .recipientName(recipientName)
            .recipientEmail(recipientEmail)
            .company(companyRef(companyId))
            .letterType(request.getLetterType())
            .referenceNumber(request.getReferenceNumber())
            .issueDate(request.getIssueDate())
            .content(content)
            .signedBy(request.getSignedBy())
            .createdBy(securityUtil.getCurrentUser())
            .issued(false)
            .build();

        letterRepository.save(letter);
        return OfferletterMapper.toLetterResponse(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferLetterResponse getById(Long id) {
        return OfferletterMapper.toLetterResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfferLetterResponse> listAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.LETTER_VIEW);
        return letterRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(OfferletterMapper::toLetterResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfferLetterResponse> listForEmployee(Long employeeId, Pageable pageable) {
        return letterRepository.findByCompanyIdAndEmployeeId(requireCompanyId(), employeeId, pageable)
            .map(OfferletterMapper::toLetterResponse);
    }

    @Override
    @Transactional
    public OfferLetterResponse issue(Long id) {
        authorizationService.checkPermission(PermissionCode.LETTER_UPDATE);
        OfferLetter letter = findInTenant(id);
        if (letter.isIssued()) throw new BadRequestException("Letter is already issued");
        letter.setIssued(true);
        return OfferletterMapper.toLetterResponse(letter);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.LETTER_DELETE);
        OfferLetter letter = findInTenant(id);
        if (letter.isIssued()) throw new BadRequestException("Cannot delete an issued letter");
        letter.softDelete();
    }

    @Override
    @Transactional(readOnly = true)
    public OfferLetterDraftResponse draftWithAi(OfferLetterDraftRequest request) {
        authorizationService.checkPermission(PermissionCode.LETTER_CREATE);
        Long companyId = requireCompanyId();

        String prompt;
        if (isPreEmploymentLetter(request.getLetterType())) {
            if (request.getJobApplicationId() == null) {
                throw new BadRequestException(
                    request.getLetterType() + " letters must be addressed to a recruitment candidate");
            }
            JobApplication application = jobApplicationRepository
                .findByIdAndCompanyId(request.getJobApplicationId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Candidate not found: " + request.getJobApplicationId()));
            prompt = buildCandidatePrompt(companyId, application, request.getLetterType().name());
        } else {
            if (request.getEmployeeId() == null) {
                throw new BadRequestException(
                    request.getLetterType() + " letters must be addressed to an employee");
            }
            Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found: " + request.getEmployeeId()));
            prompt = buildLetterPrompt(companyId, employee, request.getLetterType().name());
        }

        OfferLetterDraftResponse response = new OfferLetterDraftResponse();
        response.setContent(aiService.generateRaw(AiFeature.EMPLOYMENT_LETTER, prompt));
        return response;
    }

    private String buildCandidatePrompt(Long companyId, JobApplication application, String letterType) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        return EmploymentLetterPromptBuilder.builder()
            .setCompanyName(company.getCompanyName())
            .setEmployeeName(application.getApplicantName())
            .setDesignation(application.getJobPosting() != null ? application.getJobPosting().getTitle() : "Not specified")
            .setDepartment("Not specified")
            // The candidate hasn't joined, so there's no hire date yet — use today as
            // a placeholder proposed date for the draft (the content is editable).
            .setJoiningDate(LocalDate.now())
            .setLetterType(letterType)
            .build();
    }

    private String buildLetterPrompt(Long companyId, Employee employee, String letterType) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        return EmploymentLetterPromptBuilder.builder()
            .setCompanyName(company.getCompanyName())
            .setEmployeeName(employee.getUser().getFullName())
            .setDesignation(employee.getDesignation() != null ? employee.getDesignation().getName() : employee.getJobTitle())
            .setDepartment(employee.getDepartment() != null ? employee.getDepartment().getName() : "Not specified")
            .setJoiningDate(employee.getHireDate())
            .setLetterType(letterType)
            .build();
    }

    private OfferLetter findInTenant(Long id) {
        return letterRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Employment letter not found: " + id));
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
