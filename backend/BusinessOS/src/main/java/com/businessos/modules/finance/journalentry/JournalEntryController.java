package com.businessos.modules.finance.journalentry;

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
@RequestMapping("/api/company/finance/journal-entries")
@RequiredArgsConstructor
@Tag(name = "Journal Entries", description = "Journal Entry Management")
public class JournalEntryController {

    private final JournalEntryService service;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Create Journal Entry")
    public ResponseEntity<JournalEntryResponse> create(@Valid @RequestBody JournalEntryRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get Journal Entry by ID")
    public ResponseEntity<JournalEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Get all Journal Entries")
    public ResponseEntity<Page<JournalEntryResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Approve Journal Entry")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        service.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Post Journal Entry")
    public ResponseEntity<Void> post(@PathVariable Long id) {
        service.post(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @Operation(summary = "Delete Journal Entry")
    public ResponseEntity<JournalEntryResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
