package com.businessos.modules.finance.invoice;

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
import java.math.BigDecimal;
import com.businessos.enums.InvoiceStatus;

@RestController
@RequestMapping("/api/company/finance/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice Management")
public class ClientInvoiceController {

    private final ClientInvoiceService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Create Invoice")
    public ResponseEntity<ClientInvoiceResponse> create(@Valid @RequestBody ClientInvoiceRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Get Invoice by ID")
    public ResponseEntity<ClientInvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get the caller's own invoices")
    public ResponseEntity<Page<ClientInvoiceResponse>> getMyInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getMyInvoices(PageRequest.of(page, size)));
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Get Invoice by Number")
    public ResponseEntity<ClientInvoiceResponse> getByNumber(@PathVariable String number) {
        return ResponseEntity.ok(service.getByInvoiceNumber(number));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Get all Invoices")
    public ResponseEntity<Page<ClientInvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Get Invoices by Status")
    public ResponseEntity<Page<ClientInvoiceResponse>> getByStatus(
            @PathVariable InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getByStatus(status, PageRequest.of(page, size)));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Get Invoices by Client")
    public ResponseEntity<Page<ClientInvoiceResponse>> getByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getByClient(clientId, PageRequest.of(page, size)));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Get Overdue Invoices")
    public ResponseEntity<?> getOverdue() {
        return ResponseEntity.ok(service.getOverdueInvoices());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Update Invoice")
    public ResponseEntity<ClientInvoiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientInvoiceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Send Invoice")
    public ResponseEntity<Void> send(@PathVariable Long id) {
        service.sendInvoice(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/record-payment")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Record Payment")
    public ResponseEntity<Void> recordPayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        service.recordPayment(id, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
    @Operation(summary = "Cancel Invoice (reverses any GL posting already made)")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancelInvoice(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_OWNER')")
    @Operation(summary = "Delete Invoice")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}