package com.businessos.modules.finance.expense;

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

@RestController("financeExpenseController")
@RequestMapping("/api/company/finance/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense Management")
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create Expense")
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Get Expense by ID")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get all Expenses")
    public ResponseEntity<Page<ExpenseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get Expenses by Status")
    public ResponseEntity<Page<ExpenseResponse>> getByStatus(
            @PathVariable ExpenseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getByStatus(status, PageRequest.of(page, size)));
    }

    @GetMapping("/vendor/{vendorName}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get Expenses by Vendor")
    public ResponseEntity<Page<ExpenseResponse>> getByVendor(
            @PathVariable String vendorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getByVendorName(vendorName, PageRequest.of(page, size)));
    }

    @GetMapping("/my-expenses")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get My Expenses")
    public ResponseEntity<Page<ExpenseResponse>> getMyExpenses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getMyExpenses(employeeId, PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update Expense")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('HR_MANAGER')")
    @Operation(summary = "Approve Expense")
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @RequestParam String notes) {
        service.approveExpense(id, notes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('HR_MANAGER')")
    @Operation(summary = "Reject Expense")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestParam String reason) {
        service.rejectExpense(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/mark-as-paid")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Mark Expense as Paid")
    public ResponseEntity<Void> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) String reimbursementMethod,
            @RequestParam(required = false) String referenceNumber) {
        service.markAsPaid(id, reimbursementMethod, referenceNumber);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Delete Expense")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
