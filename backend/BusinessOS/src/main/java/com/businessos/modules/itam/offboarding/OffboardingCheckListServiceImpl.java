package com.businessos.modules.itam.offboarding;

import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffboardingCheckListServiceImpl implements OffboardingChecklistService {

    private final OffboardingChecklistRepository checklistRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public OffboardingChecklistResponse create(OffboardingChecklistRequest request) {
        Long companyId = requireCompanyId();

        // Tenant-scoped employee fetch
        Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found or doesn't belong to your company"));

        // Check if one already exists
        if (checklistRepository.findByEmployeeIdAndCompanyId(employee.getId(), companyId).isPresent()) {
            throw new BadRequestException("Offboarding checklist already exists for this employee");
        }

        OffboardingChecklist checklist = OffboardingChecklist.builder()
                .companyId(companyId)
                .employee(employee)
                .hardwareCollected(false)
                .licensesRevoked(false)
                .accessRevoked(false)
                .dataHandedOver(false)
                .exitInterviewCompleted(false)
                .overallNotes(request.getNotes()) // BUG FIX: builder previously called .notes() which does not exist
                .build();

        checklist = checklistRepository.save(checklist);
        return OffboardingChecklistMapper.toResponse(checklist);
    }

    @Override
    @Transactional(readOnly = true)
    public OffboardingChecklistResponse getById(Long id) {
        return OffboardingChecklistMapper.toResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OffboardingChecklistResponse getByEmployee(Long employeeId) {
        Long companyId = requireCompanyId();
        OffboardingChecklist checklist = checklistRepository.findByEmployeeIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Offboarding checklist not found"));
        return OffboardingChecklistMapper.toResponse(checklist);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OffboardingChecklistResponse> getAll(Pageable pageable) {
        Long companyId = requireCompanyId();
        return checklistRepository.findByCompanyId(companyId, pageable)
                .map(OffboardingChecklistMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OffboardingChecklistResponse> getPendingChecklists() {
        Long companyId = requireCompanyId();
        // BUG FIX: previously called checklistRepository.findByCompleted(false) which wasn't defined and lacked tenant isolation
        return checklistRepository.findByCompanyIdAndCompletedFalse(companyId)
                .stream()
                .map(OffboardingChecklistMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markHardwareCollected(Long id) {
        OffboardingChecklist checklist = findInTenant(id);
        checklist.setHardwareCollected(true);
        checkCompletionStatus(checklist);
        checklistRepository.save(checklist);
    }

    @Override
    @Transactional
    public void markLicensesRevoked(Long id) {
        OffboardingChecklist checklist = findInTenant(id);
        checklist.setLicensesRevoked(true);
        checkCompletionStatus(checklist);
        checklistRepository.save(checklist);
    }

    @Override
    @Transactional
    public void markAccessRevoked(Long id) {
        OffboardingChecklist checklist = findInTenant(id);
        checklist.setAccessRevoked(true);
        checkCompletionStatus(checklist);
        checklistRepository.save(checklist);
    }

    @Override
    @Transactional
    public void markDataHandedOver(Long id) {
        OffboardingChecklist checklist = findInTenant(id);
        checklist.setDataHandedOver(true);
        checkCompletionStatus(checklist);
        checklistRepository.save(checklist);
    }

    @Override
    @Transactional
    public void markExitInterviewCompleted(Long id) {
        OffboardingChecklist checklist = findInTenant(id);
        checklist.setExitInterviewCompleted(true);
        checkCompletionStatus(checklist);
        checklistRepository.save(checklist);
    }

    @Override
    @Transactional
    public OffboardingChecklistResponse delete(Long id) {
        OffboardingChecklist checklist = findInTenant(id);
        checklist.softDelete();
        checklistRepository.save(checklist);
        return OffboardingChecklistMapper.toResponse(checklist);
    }

    private void checkCompletionStatus(OffboardingChecklist checklist) {
        if (checklist.isAllTasksCompleted() && !checklist.isCompleted()) {
            checklist.setCompleted(true);
            checklist.setCompletionDate(java.time.LocalDate.now());
        }
    }

    private OffboardingChecklist findInTenant(Long id) {
        return checklistRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) {
            throw new BadRequestException("No company context found in security token");
        }
        return id;
    }
}
