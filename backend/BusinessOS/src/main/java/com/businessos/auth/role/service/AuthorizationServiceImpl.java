package com.businessos.auth.role.service;

import com.businessos.auth.role.entity.CustomRole;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.enums.Role;
import com.businessos.auth.role.repository.RolePermissionRepository;
import com.businessos.auth.user.User;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final SecurityUtil securityUtil;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void checkPermission(PermissionCode permission) {

        if (!hasPermission(permission)) {
            throw new ForbiddenException("You do not have permission: " + permission);
        }
    }

    @Override
    public boolean hasPermission(PermissionCode permission) {

        User user = securityUtil.getCurrentUser();

        // Super Admin can do everything
        if (user.getRole() == Role.SUPER_ADMIN) {
            return true;
        }

        // System Admin can only access Platform Level permissions
        if (user.getRole() == Role.SYSTEM_ADMIN) {
            return isPlatformPermission(permission);
        }

        // Company Owner can do everything inside the company
        if (user.getRole() == Role.COMPANY_OWNER) {
            return true;
        }

        CustomRole role = user.getCustomRole();
        if (role == null) {
            return false;
        }

        return rolePermissionRepository.existsByCustomRoleIdAndPermission_Code(
                role.getId(), permission.name());
    }

    private boolean isPlatformPermission(PermissionCode permission) {
        return switch (permission) {
            case COMPANY_VIEW, COMPANY_UPDATE,
                    USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE,
                    AI_ADMIN ->
                true;
            default -> false;
        };
    }
}