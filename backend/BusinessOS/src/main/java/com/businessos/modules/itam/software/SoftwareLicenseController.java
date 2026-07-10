package com.businessos.modules.itam.software;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/itam/software")
public class SoftwareLicenseController {

    private final SoftwareLicenseService licenseService;

    @PostMapping
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<SoftwareLicenseResponse> create(@Valid @RequestBody SoftwareLicenseRequest request) {
        return new ResponseEntity<>(licenseService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN') OR hasRole('EMPLOYEE')")
    public ResponseEntity<Page<SoftwareLicenseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(licenseService.getAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<SoftwareLicenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.getById(id));
    }

    @GetMapping("/key/{licenseKey}")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<SoftwareLicenseResponse> getByKey(@PathVariable String licenseKey) {
        return ResponseEntity.ok(licenseService.getByLicenseKey(licenseKey));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Page<SoftwareLicenseResponse>> getByStatus(
            @PathVariable LicenseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(licenseService.getByStatus(status, PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<SoftwareLicenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SoftwareLicenseRequest request) {
        return ResponseEntity.ok(licenseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        licenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponse>> getExpiringLicenses() {
        return ResponseEntity.ok(licenseService.getExpiringLicenses());
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<SoftwareLicenseResponse>> getExpiredLicenses() {
        return ResponseEntity.ok(licenseService.getExpiredLicenses());
    }

    @PostMapping("/{id}/assign-seat")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> assignSeat(@PathVariable Long id) {
        licenseService.assignSeat(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release-seat")
    @PreAuthorize("hasRole('IT_MANAGER') OR hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> releaseSeat(@PathVariable Long id) {
        licenseService.releaseSeat(id);
        return ResponseEntity.ok().build();
    }
}