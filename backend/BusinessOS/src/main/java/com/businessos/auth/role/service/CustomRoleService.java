package com.businessos.auth.role.service;

import com.businessos.auth.role.dto.CustomRoleRequest;
import com.businessos.auth.role.dto.CustomRoleResponse;

import java.util.List;

public interface CustomRoleService {

    CustomRoleResponse create(CustomRoleRequest request);

    CustomRoleResponse update(Long id, CustomRoleRequest request);

    void delete(Long id);

    List<CustomRoleResponse> getAll();

    CustomRoleResponse getById(Long id);

    List<String> getPermissions(Long roleId);

    void setPermissions(Long roleId, List<String> permissionCodes);

    List<String> getAllAvailablePermissions();

    void assignToUser(Long roleId, Long userId);

    void unassignFromUser(Long roleId, Long userId);
}
