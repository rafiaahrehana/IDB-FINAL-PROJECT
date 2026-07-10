package com.businessos.auth.platformuser;

import com.businessos.auth.role.enums.Role;
import com.businessos.auth.user.User;
import com.businessos.auth.user.UserMapper;
import com.businessos.auth.user.UserRepository;
import com.businessos.auth.user.UserResponse;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Same set of roles as User.isPlatformUser() - kept here since PlatformUserService
    // is the only place that needs it as a query-able list rather than a boolean check.
    private static final List<Role> PLATFORM_ROLES = List.of(
        Role.SUPER_ADMIN, Role.SYSTEM_ADMIN, Role.SUPPORT_AGENT,
        Role.SUPPORT_MANAGER, Role.MARKETING_MANAGER,
        Role.PLATFORM_ACCOUNTANT, Role.SALES_MANAGER
    );

    public UserResponse createPlatformUser(PlatformUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (!PLATFORM_ROLES.contains(request.getRole())) {
            throw new BadRequestException("Invalid platform role selected");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setActive(true);
        user.setEmailVerified(true);

        user = userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findByRoleIn(PLATFORM_ROLES, pageable).map(UserMapper::toResponse);
    }

    public UserResponse getById(Long id) {
        return UserMapper.toResponse(getPlatformUserOrThrow(id));
    }

    public UserResponse update(Long id, PlatformUserRequest request) {
        User user = getPlatformUserOrThrow(id);

        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new BadRequestException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new BadRequestException("Last name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getRole() == null || !PLATFORM_ROLES.contains(request.getRole())) {
            throw new BadRequestException("Invalid platform role selected");
        }
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

    public void deactivate(Long id) {
        User user = getPlatformUserOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private User getPlatformUserOrThrow(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Platform user not found"));
        if (!user.isPlatformUser()) {
            throw new ResourceNotFoundException("Platform user not found");
        }
        return user;
    }
}
