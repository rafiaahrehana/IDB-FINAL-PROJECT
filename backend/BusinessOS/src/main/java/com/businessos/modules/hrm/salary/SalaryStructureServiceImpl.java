package com.businessos.modules.hrm.salary;

import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.company.Company;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor

public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public SalaryStructureResponse create(SalaryStructureRequest request) {
        authorizationService.checkPermission(PermissionCode.SALARY_STRUCTURE_CREATE);
        Long companyId = requireCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        // Expire any current active structure
        salaryStructureRepository.findByEmployeeIdAndEffectiveToIsNull(request.getEmployeeId())
            .ifPresent(existing -> {
                existing.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                salaryStructureRepository.save(existing);
            });

        SalaryStructure s = SalaryStructure.builder()
            .employee(employee)
            .company(companyRef(companyId))
            .effectiveFrom(request.getEffectiveFrom())
            .grossSalary(request.getGrossSalary())
            .basicSalary(request.getBasicSalary())
            .houseRent(orZero(request.getHouseRent()))
            .medicalAllowance(orZero(request.getMedicalAllowance()))
            .transportAllowance(orZero(request.getTransportAllowance()))
            .foodAllowance(orZero(request.getFoodAllowance()))
            .specialAllowance(orZero(request.getSpecialAllowance()))
            .providentFund(orZero(request.getProvidentFund()))
            .taxDeduction(orZero(request.getTaxDeduction()))
            .notes(request.getNotes())
            .approvedBy(securityUtil.getCurrentUser())
            .build();

        salaryStructureRepository.save(s);

        employee.setBasicSalary(request.getBasicSalary());
        employee.setHouseRent(orZero(request.getHouseRent()));
        employee.setMedicalAllowance(orZero(request.getMedicalAllowance()));
        employee.setTransportAllowance(orZero(request.getTransportAllowance()));
        employee.setSalaryStructure(s);
        employeeRepository.save(employee);

        if (employee.getUser() != null) {
            try {
                Company fullCompany = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                EmailBranding.Data branding = emailBranding.from(fullCompany);
                emailService.sendSalaryRevisionEmail(employee.getUser().getEmail(), employee.getUser().getFirstName(), branding);
                
            } catch (Exception ex) {
                throw new com.businessos.shared.exception.BadRequestException("Internal error during operation: " + ex.getMessage());
            }
        }

        
        return SalaryStructureMapper.toSalaryStructureResponse(s);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse getById(Long id) {
        return SalaryStructureMapper.toSalaryStructureResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse getActiveForEmployee(Long employeeId) {
        SalaryStructure s = salaryStructureRepository.findByEmployeeIdAndEffectiveToIsNull(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("No active salary structure for employee: " + employeeId));
        return SalaryStructureMapper.toSalaryStructureResponse(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalaryStructureResponse> listForEmployee(Long employeeId, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.SALARY_STRUCTURE_VIEW);
        return salaryStructureRepository.findByCompanyIdAndEmployeeId(requireCompanyId(), employeeId, pageable)
            .map(SalaryStructureMapper::toSalaryStructureResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryStructureResponse> historyForEmployee(Long employeeId) {
        return salaryStructureRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId)
            .stream().map(SalaryStructureMapper::toSalaryStructureResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.SALARY_STRUCTURE_DELETE);
        SalaryStructure s = findInTenant(id);
        if (s.getEffectiveTo() == null) {
            throw new BadRequestException("Cannot delete the currently active salary structure. Supersede it by creating a new one.");
        }
        s.softDelete();
    }

    private SalaryStructure findInTenant(Long id) {
        return salaryStructureRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found: " + id));
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company(); c.setId(companyId); return c;
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
