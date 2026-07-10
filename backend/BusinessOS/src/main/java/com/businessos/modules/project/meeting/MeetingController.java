package com.businessos.modules.project.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
@PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<MeetingResponse> create(@RequestBody MeetingRequest request) {
        return new ResponseEntity<>(meetingService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        return ResponseEntity.ok(meetingService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeetingResponse> update(@PathVariable Long id, @RequestBody MeetingRequest request) {
        return ResponseEntity.ok(meetingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        meetingService.delete(id);
        return ResponseEntity.ok("Meeting deleted successfully");
    }
}
