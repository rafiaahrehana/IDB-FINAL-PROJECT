package com.businessos.auth.role.controller;

import com.businessos.auth.role.dto.CustomRoleRequest;
import com.businessos.auth.role.dto.CustomRoleResponse;
import com.businessos.auth.role.service.CustomRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/custom-roles")
@RequiredArgsConstructor
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    @PreAuthorize("hasRole('COMPANY_OWNER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomRoleResponse create(@Valid @RequestBody CustomRoleRequest request) {
        return customRoleService.create(request);
    }

    @GetMapping
    public List<CustomRoleResponse> getAll() {
        return customRoleService.getAll();
    }

    @GetMapping("/{id}")
    public CustomRoleResponse getById(@PathVariable Long id) {
        return customRoleService.getById(id);
    }

    @PreAuthorize("hasRole('COMPANY_OWNER')")
    @PutMapping("/{id}")
    public CustomRoleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomRoleRequest request) {

        return customRoleService.update(id, request);
    }

    @PreAuthorize("hasRole('COMPANY_OWNER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customRoleService.delete(id);
    }
}