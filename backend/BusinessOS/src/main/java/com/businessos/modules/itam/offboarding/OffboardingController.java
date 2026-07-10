package com.businessos.modules.itam.offboarding;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company/offboarding/checklist")
@RequiredArgsConstructor
public class OffboardingController {

    private final OffboardingChecklistService checklistService;

    @PostMapping
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<OffboardingChecklistResponse> createChecklist(
            @Valid @RequestBody OffboardingChecklistRequest request) {
        return new ResponseEntity<>(checklistService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN') or hasRole('IT_MANAGER')")
    public ResponseEntity<OffboardingChecklistResponse> getChecklistById(@PathVariable Long id) {
        return ResponseEntity.ok(checklistService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN') or hasRole('IT_MANAGER')")
    public ResponseEntity<OffboardingChecklistResponse> getChecklistByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(checklistService.getByEmployee(employeeId));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN') or hasRole('IT_MANAGER')")
    public ResponseEntity<Page<OffboardingChecklistResponse>> getAllChecklists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(checklistService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN') or hasRole('IT_MANAGER')")
    public ResponseEntity<List<OffboardingChecklistResponse>> getPendingChecklists() {
        return ResponseEntity.ok(checklistService.getPendingChecklists());
    }

    @PatchMapping("/{id}/hardware-collected")
    @PreAuthorize("hasRole('IT_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> markHardwareCollected(@PathVariable Long id) {
        checklistService.markHardwareCollected(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/licenses-revoked")
    @PreAuthorize("hasRole('IT_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> markLicensesRevoked(@PathVariable Long id) {
        checklistService.markLicensesRevoked(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/access-revoked")
    @PreAuthorize("hasRole('IT_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> markAccessRevoked(@PathVariable Long id) {
        checklistService.markAccessRevoked(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/data-handed-over")
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> markDataHandedOver(@PathVariable Long id) {
        checklistService.markDataHandedOver(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/exit-interview")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> markExitInterviewCompleted(@PathVariable Long id) {
        checklistService.markExitInterviewCompleted(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<OffboardingChecklistResponse> deleteChecklist(@PathVariable Long id) {
        return ResponseEntity.ok(checklistService.delete(id));
    }
}
