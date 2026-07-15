package com.businessos.modules.finance.reconciliation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/company/finance/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Bank Reconciliation", description = "Bank Reconciliation Management")
@PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
public class BankReconciliationController {

    private final BankReconciliationService service;

    @PostMapping
    @Operation(summary = "Create a Bank Reconciliation Request")
    public ResponseEntity<BankReconciliationResponse> create(@Valid @RequestBody BankReconciliationRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Bank Reconciliation by ID")
    public ResponseEntity<BankReconciliationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all Bank Reconciliations")
    public ResponseEntity<Page<BankReconciliationResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/reconcile")
    @Operation(summary = "Mark a Bank Reconciliation as Reconciled")
    public ResponseEntity<Void> markAsReconciled(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        service.markAsReconciled(id, notes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all Pending Bank Reconciliations")
    public ResponseEntity<List<BankReconciliationResponse>> getPendingReconciliations() {
        List<BankReconciliationResponse> responses = service.getPendingReconciliations()
                .stream()
                .map(BankReconciliationMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
