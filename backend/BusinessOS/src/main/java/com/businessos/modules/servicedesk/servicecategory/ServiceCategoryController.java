package com.businessos.modules.servicedesk.servicecategory;

import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FIXES:
 * 1. ServiceCategory.builder() now works — @Builder added to base.
 * 2. .sortOrder() / setSortOrder() now work — field added to base.
 * 3. Added @Valid on @RequestBody parameters.
 * 4. Added @PreAuthorize — only ADMIN manages categories.
 * 5. Removed direct import of old ServiceCategoryMapper from wrong package.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<ServiceCategoryResponse>> getAll() {
        return ResponseEntity.ok(
            categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(ServiceCategoryMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategoryResponse> getById(@PathVariable Long id) {
        ServiceCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Service category not found: " + id));
        return ResponseEntity.ok(ServiceCategoryMapper.toResponse(category));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_MANAGER')")
    @PostMapping
    @Transactional
    public ResponseEntity<ServiceCategoryResponse> create(
            @Valid @RequestBody ServiceCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName()))
            throw new BadRequestException(
                "A service category with this name already exists");
        ServiceCategory category = ServiceCategory.builder()
            .name(request.getName())
            .nameBn(request.getNameBn())
            .description(request.getDescription())
            .iconUrl(request.getIconUrl())
            .sortOrder(request.getSortOrder())
            .active(true)
            .build();
        categoryRepository.save(category);
        return new ResponseEntity<>(
            ServiceCategoryMapper.toResponse(category), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_MANAGER')")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ServiceCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceCategoryRequest request) {
        ServiceCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Service category not found: " + id));
        category.setName(request.getName());
        category.setNameBn(request.getNameBn());
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        category.setSortOrder(request.getSortOrder());
        return ResponseEntity.ok(ServiceCategoryMapper.toResponse(category));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'SUPPORT_MANAGER')")
    @PatchMapping("/{id}/toggle")
    @Transactional
    public ResponseEntity<ServiceCategoryResponse> toggle(@PathVariable Long id) {
        ServiceCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Service category not found: " + id));
        category.setActive(!category.isActive());
        return ResponseEntity.ok(ServiceCategoryMapper.toResponse(category));
    }
}
