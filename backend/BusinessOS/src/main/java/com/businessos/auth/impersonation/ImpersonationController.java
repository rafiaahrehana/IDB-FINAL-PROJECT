package com.businessos.auth.impersonation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/platform-admin")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_AGENT', 'SUPPORT_MANAGER')")
public class ImpersonationController {

    private final ImpersonationService impersonationService;

    @PostMapping("/companies/{companyId}/impersonate")
    public ResponseEntity<ImpersonationResponse> impersonate(
            @PathVariable Long companyId,
            @Valid @RequestBody ImpersonateRequest request) {
        return ResponseEntity.ok(impersonationService.startImpersonation(companyId, request));
    }

    @PostMapping("/impersonate/end")
    public ResponseEntity<Void> endImpersonation(@Valid @RequestBody EndImpersonationRequest request) {
        impersonationService.endImpersonation(request);
        return ResponseEntity.ok().build();
    }
}
