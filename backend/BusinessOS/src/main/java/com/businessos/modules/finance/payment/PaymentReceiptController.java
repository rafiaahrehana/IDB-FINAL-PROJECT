package com.businessos.modules.finance.payment;

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

@RestController
@RequestMapping("/api/company/finance/payment-receipts")
@RequiredArgsConstructor
@Tag(name = "Payment Receipts", description = "Payment Receipt Management")
public class PaymentReceiptController {

    private final PaymentReceiptService service;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Create Payment Receipt")
    public ResponseEntity<PaymentReceiptResponse> create(@Valid @RequestBody PaymentReceiptRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get Payment Receipt by ID")
    public ResponseEntity<PaymentReceiptResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get all Payment Receipts")
    public ResponseEntity<Page<PaymentReceiptResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Confirm Payment Receipt")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long id) {
        service.confirmPayment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Mark Payment Receipt as Deposited")
    public ResponseEntity<Void> markAsDeposited(
            @PathVariable Long id,
            @RequestParam String bank) {
        service.markAsDeposited(id, bank);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Delete Payment Receipt")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
