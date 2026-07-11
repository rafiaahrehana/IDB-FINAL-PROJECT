package com.businessos.auth.role.service;

import com.businessos.auth.role.dto.CustomRoleRequest;
import com.businessos.auth.role.dto.CustomRoleResponse;
import com.businessos.auth.role.entity.CustomRole;
import com.businessos.auth.role.mapper.CustomRoleMapper;
import com.businessos.auth.role.repository.CustomRoleRepository;
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
    private final com.businessos.auth.user.UserRepository userRepository;
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
}
