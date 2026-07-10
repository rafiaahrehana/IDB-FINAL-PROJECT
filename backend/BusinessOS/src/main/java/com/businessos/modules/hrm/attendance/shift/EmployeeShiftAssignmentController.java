package com.businessos.modules.hrm.attendance.shift;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hrm/attendance/shift-assignments")
@RequiredArgsConstructor
public class EmployeeShiftAssignmentController {

    private final EmployeeShiftAssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_CREATE')")
    public ResponseEntity<EmployeeShiftAssignmentResponse> create(@Valid @RequestBody EmployeeShiftAssignmentRequest request) {
        return new ResponseEntity<>(assignmentService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_READ')")
    public ResponseEntity<EmployeeShiftAssignmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_READ')")
    public ResponseEntity<Page<EmployeeShiftAssignmentResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(assignmentService.getAll(pageable));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_READ')")
    public ResponseEntity<EmployeeShiftAssignmentResponse> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(assignmentService.getByEmployee(employeeId));
    }

    @GetMapping("/shift/{shiftId}")
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_READ')")
    public ResponseEntity<Page<EmployeeShiftAssignmentResponse>> getByShift(@PathVariable Long shiftId, Pageable pageable) {
        return ResponseEntity.ok(assignmentService.getByShift(shiftId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_UPDATE')")
    public ResponseEntity<EmployeeShiftAssignmentResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeShiftAssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.update(id, request));
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_UPDATE')")
    public ResponseEntity<Void> endAssignment(@PathVariable Long id) {
        assignmentService.endAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SHIFT_ASSIGNMENT_DELETE')")
    public ResponseEntity<EmployeeShiftAssignmentResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.delete(id));
    }
}
