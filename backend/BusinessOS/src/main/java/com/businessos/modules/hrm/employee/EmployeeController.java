package com.businessos.modules.hrm.employee;

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
@RequestMapping("/api/employees")
@PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody CreateEmployeeRequest request) {
        return new ResponseEntity<>(employeeService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> listAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(employeeService.listAll(departmentId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> getMyProfile() {
        return ResponseEntity.ok(employeeService.getMyProfile());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> terminate(@PathVariable Long id) {
        employeeService.terminate(id);
        return ResponseEntity.ok("Employee terminated successfully");
    }
}


