package com.businessos.auth.user;


import com.businessos.auth.role.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String image;
    private Role role;
    private boolean active;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    private String languagePreference;
    private LocalDateTime createdAt;
    private Long customRoleId;
    private String customRoleName;
}
