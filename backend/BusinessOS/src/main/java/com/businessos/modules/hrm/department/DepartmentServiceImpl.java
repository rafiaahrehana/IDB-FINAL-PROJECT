package com.businessos.modules.hrm.department;

import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.company.Company;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtil         securityUtil;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        authorizationService.checkPermission(PermissionCode.DEPARTMENT_CREATE);
        Long companyId = requireCompanyId();

        if (departmentRepository.existsByCompanyIdAndName(companyId, request.getName())) {
            throw new BadRequestException("A department named '" + request.getName() + "' already exists");
        }

        Department dept = Department.builder()
            .name(request.getName())
            .code(request.getCode())
            .description(request.getDescription())
            .budget(request.getBudget())
            .company(companyRef(companyId))
            .build();

        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findByIdAndCompanyId(
                    request.getParentDepartmentId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Parent department not found: " + request.getParentDepartmentId()));
            dept.setDepartment(parent);
        }
        if (request.getHeadEmployeeId() != null) {
            dept.setEmployee(findEmployeeInTenant(request.getHeadEmployeeId(), companyId));
        }

        departmentRepository.save(dept);
        
        return DepartmentMapper.toResponse(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return DepartmentMapper.toResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> listAll(Pageable pageable) {
        // Deliberately NOT gated by DEPARTMENT_VIEW: this endpoint doubles as a
        // cross-module picker (Announcements, Holidays, Job Postings all use it to
        // populate a department dropdown) for users who may lack DEPARTMENT_VIEW but
        // hold whatever permission actually governs that other module. The HRM
        // "Departments" admin page is gated at the frontend sidebar/route level only,
        // until this endpoint is split into a full admin view vs a lightweight picker.
        return departmentRepository.findByCompanyId(requireCompanyId(), pageable)
            .map(DepartmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> listActive() {
        return departmentRepository.findByCompanyIdAndActiveTrue(requireCompanyId())
            .stream().map(DepartmentMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        authorizationService.checkPermission(PermissionCode.DEPARTMENT_UPDATE);
        Long companyId = requireCompanyId();
        Department dept = findInTenant(id);

        if (!dept.getName().equals(request.getName())
                && departmentRepository.existsByCompanyIdAndName(companyId, request.getName())) {
            throw new BadRequestException("A department named '" + request.getName() + "' already exists");
        }

        dept.setName(request.getName());
        if (request.getCode()        != null) dept.setCode(request.getCode());
        if (request.getDescription() != null) dept.setDescription(request.getDescription());
        if (request.getBudget()      != null) dept.setBudget(request.getBudget());

        if (request.getParentDepartmentId() != null) {
            if (request.getParentDepartmentId().equals(id)) {
                throw new BadRequestException("A department cannot be its own parent");
            }
            dept.setDepartment(departmentRepository.findByIdAndCompanyId(
                    request.getParentDepartmentId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Parent department not found: " + request.getParentDepartmentId())));
        } else {
            dept.setDepartment(null);
        }

        if (request.getHeadEmployeeId() != null) {
            dept.setEmployee(findEmployeeInTenant(request.getHeadEmployeeId(), companyId));
        } else {
            dept.setEmployee(null);
        }

        return DepartmentMapper.toResponse(dept);
    }

    @Override
    @Transactional
    public DepartmentResponse toggleActive(Long id) {
        authorizationService.checkPermission(PermissionCode.DEPARTMENT_UPDATE);
        Department dept = findInTenant(id);
        dept.setActive(!dept.isActive());
        return DepartmentMapper.toResponse(dept);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.DEPARTMENT_DELETE);
        Department dept = findInTenant(id);
        if (!dept.getEmployees().isEmpty()) {
            throw new BadRequestException(
                "Cannot delete a department that has employees. Reassign employees first.");
        }
        dept.softDelete();
    }

    private Department findInTenant(Long id) {
        return departmentRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private Employee findEmployeeInTenant(Long employeeId, Long companyId) {
        return employeeRepository.findByIdAndCompanyId(employeeId, companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
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
