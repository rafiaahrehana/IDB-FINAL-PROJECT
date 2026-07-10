package com.businessos.modules.itam.assetimport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/itam/asset-import")
@PreAuthorize("hasAnyRole('COMPANY_OWNER', 'EMPLOYEE')")
public class AssetImportController {

    private final AssetImportService assetImportService;

    @GetMapping(value = "/template", produces = "text/csv")
    public ResponseEntity<String> getTemplate() {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=asset-import-template.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(assetImportService.getTemplateCsv());
    }

    @PostMapping
    public ResponseEntity<AssetImportResultResponse> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assetImportService.importCsv(file));
    }
}
