package com.businessos.core.subscription;

import com.businessos.enums.CompanyStatus;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionManagementController {

    private final SubscriptionPlanConfigRepository planConfigRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanConfigResponse>> getAllPlans() {
        return ResponseEntity.ok(planConfigRepository.findAllByOrderByPlanAsc()
                .stream().map(this::toConfigResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanConfigResponse> getPlan(@PathVariable Long id) {
        SubscriptionPlanConfig config = planConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        return ResponseEntity.ok(toConfigResponse(config));
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlanConfigResponse> createPlan(
            @Valid @RequestBody SubscriptionPlanConfigRequest request) {
        if (planConfigRepository.findByPlan(request.getPlan()).isPresent()) {
            throw new BadRequestException("Plan " + request.getPlan() + " already exists");
        }
        SubscriptionPlanConfig config = toEntity(request);
        return ResponseEntity.ok(toConfigResponse(planConfigRepository.save(config)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlanConfigResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanConfigRequest request) {
        SubscriptionPlanConfig config = planConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        config.setDisplayName(request.getDisplayName());
        config.setDescription(request.getDescription());
        config.setPrice(request.getPrice());
        config.setCurrency(request.getCurrency());
        config.setDurationDays(request.getDurationDays());
        config.setFeatured(request.isFeatured());
        config.setActive(request.isActive());
        config.setMaxEmployees(request.getMaxEmployees());
        config.setMaxClients(request.getMaxClients());
        config.setMaxProjects(request.getMaxProjects());
        config.setMaxStorageMb(request.getMaxStorageMb());
        config.setAiEnabled(request.isAiEnabled());
        config.setWebsiteBuilderEnabled(request.isWebsiteBuilderEnabled());
        config.setCustomDomainEnabled(request.isCustomDomainEnabled());
        config.setPrioritySupport(request.isPrioritySupport());
        config.setApiAccess(request.isApiAccess());
        config.setFeatures(request.getFeatures());
        return ResponseEntity.ok(toConfigResponse(planConfigRepository.save(config)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        SubscriptionPlanConfig config = planConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        planConfigRepository.delete(config);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @GetMapping("/company-subscriptions")
    public ResponseEntity<List<Map<String, Object>>> getAllCompanySubscriptions() {
        List<Subscription> all = subscriptionRepository.findAll();
        List<Map<String, Object>> result = all.stream().map(s -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", s.getId());
            map.put("companyId", s.getCompanyId());
            map.put("plan", s.getPlan().name());
            map.put("amount", s.getAmount());
            map.put("currency", s.getCurrency());
            map.put("startDate", s.getStartDate());
            map.put("endDate", s.getEndDate());
            map.put("status", s.getStatus().name());
            map.put("paymentMethod", s.getPaymentMethod());

            Company company = companyRepository.findById(s.getCompanyId()).orElse(null);
            if (company != null) {
                map.put("companyName", company.getCompanyName());
                map.put("companyStatus", company.getStatus().name());
            }
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/suspend/{companyId}")
    public ResponseEntity<Map<String, String>> suspendCompany(@PathVariable Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setStatus(CompanyStatus.SUSPENDED);
        companyRepository.save(company);
        return ResponseEntity.ok(Map.of("status", "suspended", "companyId", companyId.toString()));
    }

    @PostMapping("/activate/{companyId}")
    public ResponseEntity<Map<String, String>> activateCompany(@PathVariable Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company);
        return ResponseEntity.ok(Map.of("status", "activated", "companyId", companyId.toString()));
    }

    private SubscriptionPlanConfig toEntity(SubscriptionPlanConfigRequest r) {
        return SubscriptionPlanConfig.builder()
                .plan(r.getPlan())
                .displayName(r.getDisplayName())
                .description(r.getDescription())
                .price(r.getPrice())
                .currency(r.getCurrency() != null ? r.getCurrency() : "BDT")
                .durationDays(r.getDurationDays())
                .featured(r.isFeatured())
                .active(r.isActive())
                .maxEmployees(r.getMaxEmployees())
                .maxClients(r.getMaxClients())
                .maxProjects(r.getMaxProjects())
                .maxStorageMb(r.getMaxStorageMb())
                .aiEnabled(r.isAiEnabled())
                .websiteBuilderEnabled(r.isWebsiteBuilderEnabled())
                .customDomainEnabled(r.isCustomDomainEnabled())
                .prioritySupport(r.isPrioritySupport())
                .apiAccess(r.isApiAccess())
                .features(r.getFeatures())
                .build();
    }

    private SubscriptionPlanConfigResponse toConfigResponse(SubscriptionPlanConfig c) {
        SubscriptionPlanConfigResponse r = new SubscriptionPlanConfigResponse();
        r.setId(c.getId());
        r.setPlan(c.getPlan().name());
        r.setDisplayName(c.getDisplayName());
        r.setDescription(c.getDescription());
        r.setPrice(c.getPrice());
        r.setCurrency(c.getCurrency());
        r.setDurationDays(c.getDurationDays());
        r.setFeatured(c.isFeatured());
        r.setActive(c.isActive());
        r.setMaxEmployees(c.getMaxEmployees());
        r.setMaxClients(c.getMaxClients());
        r.setMaxProjects(c.getMaxProjects());
        r.setMaxStorageMb(c.getMaxStorageMb());
        r.setAiEnabled(c.isAiEnabled());
        r.setWebsiteBuilderEnabled(c.isWebsiteBuilderEnabled());
        r.setCustomDomainEnabled(c.isCustomDomainEnabled());
        r.setPrioritySupport(c.isPrioritySupport());
        r.setApiAccess(c.isApiAccess());
        r.setFeatures(c.getFeatures());
        return r;
    }
}
