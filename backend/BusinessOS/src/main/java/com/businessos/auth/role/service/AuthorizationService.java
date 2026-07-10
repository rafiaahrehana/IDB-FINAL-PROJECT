package com.businessos.auth.role.service;

import com.businessos.auth.role.enums.PermissionCode;

public interface AuthorizationService {

    void checkPermission(PermissionCode permission);

    boolean hasPermission(PermissionCode permission);
}