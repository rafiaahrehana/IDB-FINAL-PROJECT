package com.businessos.modules.hrm.performance;

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
@RequestMapping("/api/hr/performance")
@PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public ResponseEntity<PerformanceReviewResponse> create(@RequestBody PerformanceReviewRequest request) {
        return new ResponseEntity<>(performanceService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PerformanceReviewResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(performanceService.listAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<PerformanceReviewResponse>> listForEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(performanceService.listForEmployee(employeeId,
                PageRequest.of(page, size, Sort.by("reviewPeriodStart").descending())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PerformanceReviewResponse> update(
            @PathVariable Long id,
            @RequestBody PerformanceReviewRequest request) {
        return ResponseEntity.ok(performanceService.update(id, request));
    }

    @PatchMapping("/{id}/finalise")
    public ResponseEntity<PerformanceReviewResponse> finalise(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.finalise(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        performanceService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}


