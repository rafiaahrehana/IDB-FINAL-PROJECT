package com.businessos.shared.location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Location and GeoNode Management")
public class GeoNodeController {

    private final GeoNodeService geoNodeService;

    @GetMapping("/countries")
    @Operation(summary = "Get all countries")
    public ResponseEntity<List<GeoNodeDto>> getCountries() {
        return ResponseEntity.ok(geoNodeService.getCountries());
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "Get child nodes by parent ID")
    public ResponseEntity<List<GeoNodeDto>> getChildren(@PathVariable Long parentId) {
        return ResponseEntity.ok(geoNodeService.getChildren(parentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get node by ID")
    public ResponseEntity<GeoNodeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(geoNodeService.getNodeById(id));
    }

    // Mutating endpoints require platform-admin auth even though GETs on this
    // path are public (see SecurityConfig - only GET /api/locations/** is
    // permitAll, these three are not covered by that rule).
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new location node")
    public ResponseEntity<GeoNodeDto> create(@Valid @RequestBody CreateGeoNodeRequest request) {
        return new ResponseEntity<>(geoNodeService.createNode(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing location node")
    public ResponseEntity<GeoNodeDto> update(@PathVariable Long id, @Valid @RequestBody UpdateGeoNodeRequest request) {
        return ResponseEntity.ok(geoNodeService.updateNode(id, request));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a location node")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        geoNodeService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }
}

