package com.businessos.modules.hrm.recruitment.offerletter;

import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.service.AiService;

@Service
@RequiredArgsConstructor

public class OfferLetterServiceImpl implements OfferLetterService {

    private final OfferLetterRepository letterRepository;
    private final EmployeeRepository         employeeRepository;
    private final SecurityUtil               securityUtil;
    private final AiService                  aiService;
    private final AuthorizationService       authorizationService;

    @Override
    @Transactional
    public OfferLetterResponse create(OfferLetterRequest request) {
        authorizationService.checkPermission(PermissionCode.LETTER_CREATE);
        Long companyId = requireCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        if (request.getReferenceNumber() != null
                && letterRepository.existsByCompanyIdAndReferenceNumber(companyId, request.getReferenceNumber())) {
            throw new BadRequestException("Reference number already exists: " + request.getReferenceNumber());
        }

        String content = request.getContent();
        if (content == null || content.isBlank()) {
            String prompt = "Generate an employment letter of type: " + request.getLetterType() + 
                            " for employee " + employee.getUser().getFirstName() + " " + employee.getUser().getLastName() + 
                            ". Make sure it is professional and includes placeholders for salary, start date, and terms of employment.";
            content = aiService.generateFromPrompt(AiFeature.EMPLOYMENT_LETTER, prompt);
        }

        OfferLetter letter = OfferLetter.builder()
            .employee(employee)
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
