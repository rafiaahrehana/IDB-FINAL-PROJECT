package com.businessos.auth.role.service;

import com.businessos.auth.role.dto.CustomRoleRequest;
import com.businessos.auth.role.dto.CustomRoleResponse;
import com.businessos.auth.role.entity.CustomRole;
import com.businessos.auth.role.entity.Permission;
import com.businessos.auth.role.entity.RolePermission;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.mapper.CustomRoleMapper;
import com.businessos.auth.role.repository.CustomRoleRepository;
import com.businessos.auth.role.repository.PermissionRepository;
import com.businessos.auth.role.repository.RolePermissionRepository;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserRepository;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
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
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
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

        rolePermissionRepository.deleteByCustomRoleId(role.getId());
        userRepository.clearCustomRoleForAllUsers(role.getId());

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
        Long companyId = securityUtil.getCurrentCompanyId();
        CustomRole role = customRoleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
        return rolePermissionRepository.findByCustomRoleId(roleId)
                .stream()
                .map(rp -> rp.getPermission().getCode())
                .toList();
    }

    @Override
    public void setPermissions(Long roleId, List<String> permissionCodes) {
        Long companyId = securityUtil.getCurrentCompanyId();
        CustomRole role = customRoleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new BadRequestException("System roles cannot be modified.");
        }
        rolePermissionRepository.deleteByCustomRoleId(roleId);
        for (String code : permissionCodes) {
            Permission perm = permissionRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Invalid permission: " + code));
            RolePermission rp = RolePermission.builder()
                    .customRole(role)
                    .permission(perm)
                    .build();
            rolePermissionRepository.save(rp);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllAvailablePermissions() {
        return java.util.Arrays.stream(PermissionCode.values())
                .map(Enum::name)
                .toList();
    }

    @Override
    public void assignToUser(Long roleId, Long userId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        CustomRole role = customRoleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setCustomRole(role);
        userRepository.save(user);
    }

    @Override
    public void unassignFromUser(Long roleId, Long userId) {
        Long companyId = securityUtil.getCurrentCompanyId();
        customRoleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getCustomRole() != null && user.getCustomRole().getId().equals(roleId)) {
            user.setCustomRole(null);
            userRepository.save(user);
        }
    }
}
