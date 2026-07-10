package com.businessos.modules.ai.controller;

import jakarta.validation.Valid;

import com.businessos.modules.ai.dto.request.AiGenerateRequest;
import com.businessos.modules.ai.dto.request.AiPromptTemplateRequest;
import com.businessos.modules.ai.dto.request.AiProviderConfigRequest;
import com.businessos.modules.ai.dto.response.AiGenerateResponse;
import com.businessos.modules.ai.dto.response.AiPromptTemplateResponse;
import com.businessos.modules.ai.dto.response.AiProviderConfigResponse;
import com.businessos.modules.ai.dto.response.AiUsageSummaryResponse;
import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.service.AiService;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final AuthorizationService authorizationService;

    @PostMapping("/generate")
    public ResponseEntity<AiGenerateResponse> generate(@Valid @RequestBody AiGenerateRequest request) {
        return new ResponseEntity<>(aiService.generate(request), HttpStatus.CREATED);
    }

    @GetMapping("/conversations")
    public ResponseEntity<Page<AiGenerateResponse>> conversations(
            @RequestParam(required = false) AiFeature feature,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(aiService.listConversations(feature,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/usage")
    public ResponseEntity<AiUsageSummaryResponse> usage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        authorizationService.checkPermission(PermissionCode.AI_ADMIN);
        return ResponseEntity.ok(aiService.getUsageSummary(date));
    }

    @PostMapping("/config")
    public ResponseEntity<AiProviderConfigResponse> saveConfig(@Valid @RequestBody AiProviderConfigRequest request) {
        authorizationService.checkPermission(PermissionCode.AI_ADMIN);
        return ResponseEntity.ok(aiService.saveProviderConfig(request));
    }

    @GetMapping("/config")
    public ResponseEntity<AiProviderConfigResponse> getConfig() {
        authorizationService.checkPermission(PermissionCode.AI_ADMIN);
        return ResponseEntity.ok(aiService.getProviderConfig());
    }

    @PostMapping("/templates")
    public ResponseEntity<AiPromptTemplateResponse> saveTemplate(@Valid @RequestBody AiPromptTemplateRequest request) {
        authorizationService.checkPermission(PermissionCode.AI_ADMIN);
        return new ResponseEntity<>(aiService.savePromptTemplate(request), HttpStatus.CREATED);
    }

    @GetMapping("/templates")
    public ResponseEntity<Page<AiPromptTemplateResponse>> listTemplates(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        authorizationService.checkPermission(PermissionCode.AI_ADMIN);
        return ResponseEntity.ok(aiService.listPromptTemplates(
                PageRequest.of(page, size, Sort.by("feature").ascending())));
    }
}
