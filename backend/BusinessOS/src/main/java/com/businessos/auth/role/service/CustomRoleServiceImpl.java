package com.businessos.auth.role.service;

import com.businessos.auth.role.dto.CustomRoleRequest;
import com.businessos.auth.role.dto.CustomRoleResponse;
import com.businessos.auth.role.entity.CustomRole;
import com.businessos.auth.role.entity.Permission;
import com.businessos.auth.role.entity.RolePermission;
import com.businessos.auth.role.mapper.CustomRoleMapper;
import com.businessos.auth.role.repository.CustomRoleRepository;
import com.businessos.auth.role.repository.PermissionRepository;
import com.businessos.auth.role.repository.RolePermissionRepository;
import com.businessos.auth.user.User;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomRoleServiceImpl implements CustomRoleService {

    private final CustomRoleRepository customRoleRepository;
    private final CompanyRepository companyRepository;
    private final com.businessos.auth.user.UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtil securityUtil;

    @Override
    public CustomRoleResponse create(CustomRoleRequest request) {

        Long companyId = securityUtil.getCurrentCompanyId();

        if (customRoleRepository.existsByCompanyIdAndNameIgnoreCase(companyId, request.getName())) {
            throw new BadRequestException("Role already exists.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        CustomRole role = CustomRoleMapper.toEntity(request);
        role.setCompany(company);
        role = customRoleRepository.save(role);
        return CustomRoleMapper.toResponse(role);
    }

    @Override
    public CustomRoleResponse update(Long id, CustomRoleRequest request) {

        Long companyId = securityUtil.getCurrentCompanyId();

        CustomRole role = customRoleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BadRequestException("System roles cannot be updated.");
        }

        // Guard against renaming to an existing role name within the same company
        if (!role.getName().equalsIgnoreCase(request.getName()) &&
            customRoleRepository.existsByCompanyIdAndNameIgnoreCase(companyId, request.getName())) {
            throw new BadRequestException("A role with that name already exists.");
        }

        role.setName(request.getName().trim());
        role.setDescription(request.getDescription());

        return CustomRoleMapper.toResponse(role);
    }

    @Override
    public void delete(Long id) {

        Long companyId = securityUtil.getCurrentCompanyId();

        CustomRole role = customRoleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BadRequestException("System roles cannot be deleted.");
        }

        // Nullify customRole FK on all users before soft-deleting.
        // Replaces the invalid CascadeType.SET_NULL that was removed from User.customRole.
        userRepository.clearCustomRoleForAllUsers(role.getId());
        rolePermissionRepository.deleteByCustomRoleId(role.getId());

        role.softDelete();
        customRoleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomRoleResponse> getAll() {

        Long companyId = securityUtil.getCurrentCompanyId();

        return customRoleRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(CustomRoleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomRoleResponse getById(Long id) {

        Long companyId = securityUtil.getCurrentCompanyId();

        CustomRole role = customRoleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        return CustomRoleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPermissions(Long roleId) {
        CustomRole role = findInTenant(roleId);
        return rolePermissionRepository.findByCustomRoleId(role.getId())
                .stream()
                .map(rp -> rp.getPermission().getCode())
                .toList();
    }

    @Override
    public List<String> setPermissions(Long roleId, List<String> permissionCodes) {
        CustomRole role = findInTenant(roleId);
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BadRequestException("System roles cannot be modified.");
        }

        rolePermissionRepository.deleteByCustomRoleId(role.getId());

        List<String> codes = permissionCodes == null ? List.of() : permissionCodes;
        for (String code : codes) {
            Permission permission = permissionRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Unknown permission: " + code));
            rolePermissionRepository.save(RolePermission.builder()
                    .customRole(role)
                    .permission(permission)
                    .build());
        }

        return getPermissions(roleId);
    }

    @Override
    public void assignEmployee(Long roleId, Long employeeId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        CustomRole role = findInTenant(roleId);

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        User user = employee.getUser();
        if (user == null) {
            throw new BadRequestException("Employee has no linked user account");
        }
        user.setCustomRole(role);
        userRepository.save(user);
    }

    @Override
    public void unassignEmployee(Long employeeId) {
        Long companyId = securityUtil.getCurrentCompanyId();

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        User user = employee.getUser();
        if (user == null) {
            throw new BadRequestException("Employee has no linked user account");
        }
        user.setCustomRole(null);
        userRepository.save(user);
    }

    private CustomRole findInTenant(Long roleId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return customRoleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
    }
}
