package com.businessos.auth.user;

import com.businessos.shared.address.Address;
import com.businessos.shared.address.AddressMapper;
import com.businessos.shared.address.AddressRequest;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileResponse> getProfile() {
        User principal = securityUtil.getCurrentUser();
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(mapToResponse(user));
    }

    @PatchMapping("/profile")
    @Transactional
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UserProfileRequest request) {
        User principal = securityUtil.getCurrentUser();
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getImage() != null) {
            user.setImage(request.getImage().trim());
        }
        if (request.getLanguagePreference() != null) {
            user.setLanguagePreference(request.getLanguagePreference().trim());
        }

        // Handle Address update
        if (request.getLocation() != null) {
            AddressRequest locReq = request.getLocation();
            if (user.getLocation() == null) {
                Address newLoc = addressMapper.toEntity(locReq);
                user.setLocation(newLoc);
            } else {
                addressMapper.updateEntityFromRequest(user.getLocation(), locReq);
            }
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(mapToResponse(savedUser));
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .image(user.getImage())
                .role(user.getRole())
                .languagePreference(user.getLanguagePreference())
                .location(addressMapper.toResponse(user.getLocation()))
                .build();
    }
}
