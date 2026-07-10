package com.businessos.modules.company;

import com.businessos.enums.CompanyStatus;
import com.businessos.core.subscription.SubscriptionPlan;
import com.businessos.shared.exception.UnauthorizedException;
import com.businessos.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final SecurityUtil   securityUtil;

    @GetMapping("/public/{subdomain}")
    public ResponseEntity<CompanyPublicResponse> getPublic(@PathVariable String subdomain) {
        return ResponseEntity.ok(companyService.getBySubdomain(subdomain));
    }

    @GetMapping("/me")
    public ResponseEntity<CompanyResponse> getMyCompany() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null)
            throw new UnauthorizedException("No company associated with this account");
        return ResponseEntity.ok(companyService.getById(companyId));
    }

    @PatchMapping("/me")
    public ResponseEntity<CompanyResponse> updateMyCompany(@Valid @RequestBody UpdateCompanyRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null)
            throw new UnauthorizedException("No company associated with this account");
        return ResponseEntity.ok(companyService.update(companyId, request));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SALES_MANAGER', 'PLATFORM_ACCOUNTANT', 'SUPPORT_AGENT', 'SUPPORT_MANAGER', 'MARKETING_MANAGER', 'ACCOUNT_EXECUTIVE')")
    @PostMapping("/admin")
    public ResponseEntity<CompanyResponse> registerByAdmin(@Valid @RequestBody RegisterCompanyRequest request) {
        return new ResponseEntity<>(companyService.registerByAdmin(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SALES_MANAGER', 'PLATFORM_ACCOUNTANT', 'SUPPORT_AGENT', 'SUPPORT_MANAGER', 'MARKETING_MANAGER', 'ACCOUNT_EXECUTIVE')")
    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> listAll(
            @RequestParam(required = false) CompanyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(companyService.listAll(status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SALES_MANAGER', 'PLATFORM_ACCOUNTANT', 'SUPPORT_AGENT', 'SUPPORT_MANAGER', 'MARKETING_MANAGER', 'ACCOUNT_EXECUTIVE')")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SALES_MANAGER', 'PLATFORM_ACCOUNTANT', 'ACCOUNT_EXECUTIVE')")
    @PatchMapping("/{id}/plan")
    public ResponseEntity<CompanyResponse> changePlan(
            @PathVariable Long id,
            @RequestParam SubscriptionPlan plan) {
        return ResponseEntity.ok(companyService.changePlan(id, plan));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'PLATFORM_ACCOUNTANT', 'ACCOUNT_EXECUTIVE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CompanyResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam CompanyStatus status) {
        return ResponseEntity.ok(companyService.changeStatus(id, status));
    }

    @PatchMapping("/me/deactivate")
    public ResponseEntity<String> deactivateMyCompany() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null)
            throw new UnauthorizedException("No company associated with this account");
        companyService.deactivateByOwner(companyId);
        return ResponseEntity.ok("Company deactivated successfully");
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyCompany() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null)
            throw new UnauthorizedException("No company associated with this account");
        companyService.deleteByOwner(companyId);
        return ResponseEntity.ok("Company deleted successfully");
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<String> suspendCompany(@PathVariable Long id) {
        companyService.suspendByAdmin(id);
        return ResponseEntity.ok("Company suspended successfully");
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateCompany(@PathVariable Long id) {
        companyService.activateByAdmin(id);
        return ResponseEntity.ok("Company activated successfully");
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivate(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.ok("Company deleted successfully");
    }
}
