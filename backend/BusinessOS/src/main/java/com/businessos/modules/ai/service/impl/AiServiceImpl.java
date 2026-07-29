package com.businessos.modules.ai.service.impl;

import com.businessos.modules.ai.audit.AiAuditService;
import com.businessos.modules.ai.dto.request.AiGenerateRequest;
import com.businessos.modules.ai.dto.request.AiPromptTemplateRequest;
import com.businessos.modules.ai.dto.request.AiProviderConfigRequest;
import com.businessos.modules.ai.dto.response.AiGenerateResponse;
import com.businessos.modules.ai.dto.response.AiPromptTemplateResponse;
import com.businessos.modules.ai.dto.response.AiProviderConfigResponse;
import com.businessos.modules.ai.dto.response.AiUsageSummaryResponse;
import com.businessos.modules.ai.entity.AiConversation;
import com.businessos.modules.ai.entity.AiPromptTemplate;
import com.businessos.modules.ai.entity.AiProviderConfig;
import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.exception.AiProviderException;
import com.businessos.modules.ai.exception.AiQuotaExceededException;
import com.businessos.modules.ai.mapper.AiMapper;
import com.businessos.modules.ai.provider.AiProviderAdapter;
import com.businessos.modules.ai.repository.AiConversationRepository;
import com.businessos.modules.ai.repository.AiPromptTemplateRepository;
import com.businessos.modules.ai.repository.AiProviderConfigRepository;
import com.businessos.modules.ai.repository.AiUsageLogRepository;
import com.businessos.modules.ai.resolver.AiProviderResolver;
import com.businessos.modules.ai.service.AiService;
import com.businessos.modules.ai.util.AiKeyDecryptor;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.auth.user.User;
import com.businessos.modules.company.Company;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiProviderResolver         resolver;
    private final AiAuditService             auditService;
    private final AiUsageLogRepository       usageLogRepository;
    private final AiProviderConfigRepository configRepository;
    private final AiPromptTemplateRepository templateRepository;
    private final AiConversationRepository   conversationRepository; // FIX: was missing
    private final AiKeyDecryptor             keyDecryptor;
    private final SecurityUtil               securityUtil;
    private final AuthorizationService       authorizationService;

    @Override
    @Transactional
    public AiGenerateResponse generate(AiGenerateRequest request) {
        authorizationService.checkPermission(PermissionCode.AI_CHAT);
        User user      = securityUtil.getCurrentUser();
        Long companyId = securityUtil.getCurrentCompanyId();
        Company company = companyRef(companyId);

        enforceRateLimits(companyId, user.getId());

        String prompt = resolvePrompt(request.getFeature(), request.getPrompt(), companyId);
        AiProviderAdapter adapter = resolver.resolve(companyId);

        long start    = System.currentTimeMillis();
        String result = generateWithRetry(adapter, prompt);
        long elapsed  = System.currentTimeMillis() - start;

        String uuid = auditService.record(
            request.getFeature(), adapter.getProviderType(), adapter.getModel(),
            prompt, result, elapsed, user, company);

        

        AiGenerateResponse response = new AiGenerateResponse();
        response.setConversationUuid(uuid);
        response.setFeature(request.getFeature());
        response.setProvider(adapter.getProviderType());
        response.setModel(adapter.getModel());
        response.setResult(result);
        response.setExecutionTimeMs(elapsed);
        return response;
    }

    @Override
    @Transactional
    public String generateFromPrompt(AiFeature feature, String prompt) {
        AiGenerateRequest request = new AiGenerateRequest();
        request.setFeature(feature);
        request.setPrompt(prompt);
        return generate(request).getResult();
    }

    @Override
    @Transactional
    public String generateRaw(AiFeature feature, String prompt) {
        authorizationService.checkPermission(PermissionCode.AI_CHAT);
        User user       = securityUtil.getCurrentUser();
        Long companyId  = securityUtil.getCurrentCompanyId();
        Company company = companyRef(companyId);

        enforceRateLimits(companyId, user.getId());

        AiProviderAdapter adapter = resolver.resolve(companyId);

        long start    = System.currentTimeMillis();
        String result = generateWithRetry(adapter, prompt);
        long elapsed  = System.currentTimeMillis() - start;

        auditService.record(feature, adapter.getProviderType(), adapter.getModel(),
            prompt, result, elapsed, user, company);

        return result;
    }

    @Override
    @Transactional
    public AiProviderConfigResponse saveProviderConfig(AiProviderConfigRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        // Upsert by (companyId, provider) - a company can save one config per
        // provider (uq_ai_config_company_provider). Previously this looked up
        // the single *active* row regardless of provider, so saving a second
        // provider (e.g. Gemini alongside an already-saved Claude) silently
        // overwrote the Claude row instead of creating its own.
        AiProviderConfig config = configRepository
            .findByCompanyIdAndAiProviderType(companyId, request.getAiProviderType())
            .orElseGet(() -> {
                AiProviderConfig c = new AiProviderConfig();
                c.setCompany(companyRef(companyId));
                c.setAiProviderType(request.getAiProviderType());
                return c;
            });

        config.setAiModel(request.getModel());

        // Trimmed before encrypting - a copy-pasted key very commonly carries an
        // invisible leading/trailing newline or space, which the provider's API
        // rejects outright (e.g. Anthropic's "invalid x-api-key") with no hint
        // that whitespace, not the key itself, was the problem.
        if (request.getApiKey() != null && !request.getApiKey().isBlank())
            config.setApiKeyEncrypted(keyDecryptor.encrypt(request.getApiKey().trim()));
        if (request.getTemperature() != null)
            config.setTemperature(request.getTemperature());
        if (request.getMaxTokens() != null)
            config.setMaxTokens(request.getMaxTokens());

        // Saving a config is "I want to use this now" - make it the active one
        // and deactivate whichever provider was active before, so exactly one
        // config drives AiProviderResolver.resolve() at all times.
        deactivateAllExcept(companyId, null);
        config.setActive(true);
        configRepository.save(config);

        return AiMapper.toConfigResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public AiProviderConfigResponse getProviderConfig() {
        return configRepository.findByCompanyIdAndActiveTrue(securityUtil.getCurrentCompanyId())
            .map(AiMapper::toConfigResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No AI provider config found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiProviderConfigResponse> listProviderConfigs() {
        return configRepository.findByCompanyIdOrderByAiProviderType(securityUtil.getCurrentCompanyId())
            .stream()
            .map(AiMapper::toConfigResponse)
            .toList();
    }

    @Override
    @Transactional
    public AiProviderConfigResponse activateProviderConfig(Long id) {
        Long companyId = securityUtil.getCurrentCompanyId();
        AiProviderConfig config = configRepository.findById(id)
            .filter(c -> c.getCompany() != null && companyId.equals(c.getCompany().getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Provider config not found: " + id));

        deactivateAllExcept(companyId, id);
        config.setActive(true);
        configRepository.save(config);
        return AiMapper.toConfigResponse(config);
    }

    @Override
    @Transactional
    public void deleteProviderConfig(Long id) {
        Long companyId = securityUtil.getCurrentCompanyId();
        AiProviderConfig config = configRepository.findById(id)
            .filter(c -> c.getCompany() != null && companyId.equals(c.getCompany().getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Provider config not found: " + id));

        if (config.isActive()) {
            throw new com.businessos.shared.exception.BadRequestException(
                "Cannot delete the active provider - activate a different one first.");
        }
        configRepository.delete(config);
    }

    /** Deactivates every saved config for the company except (optionally) the one given. */
    private void deactivateAllExcept(Long companyId, Long keepId) {
        for (AiProviderConfig c : configRepository.findByCompanyIdOrderByAiProviderType(companyId)) {
            if (c.isActive() && !c.getId().equals(keepId)) {
                c.setActive(false);
                configRepository.save(c);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiGenerateResponse> listConversations(AiFeature feature, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.AI_CHAT);
        Long companyId = securityUtil.getCurrentCompanyId();

        /*
         * FIX: original code queried templateRepository (prompt templates) instead of
         * conversationRepository (actual AI call history). Both branches of the ternary
         * also did identical queries — the feature filter was completely ignored.
         *
         * Corrected to use conversationRepository with proper feature branching.
         */
        Page<AiConversation> page = (feature != null)
            ? conversationRepository.findByCompanyIdAndFeatureOrderByCreatedAtDesc(
                companyId, feature, pageable)
            : conversationRepository.findByCompanyIdOrderByCreatedAtDesc(
                companyId, pageable);

        return page.map(conv -> {
            AiGenerateResponse r = new AiGenerateResponse();
            r.setConversationUuid(conv.getConversationUuid());
            r.setFeature(conv.getFeature());
            r.setProvider(conv.getProvider());
            r.setModel(conv.getModel());
            r.setResult(conv.getResponsePayload());
            r.setExecutionTimeMs(conv.getExecutionTimeMs() != null
                ? conv.getExecutionTimeMs() : 0L);
            return r;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AiUsageSummaryResponse getUsageSummary(LocalDate date) {
        Long companyId   = securityUtil.getCurrentCompanyId();
        LocalDate target = date != null ? date : LocalDate.now();

        long totalRequests = usageLogRepository.countByCompanyAndDate(companyId, target);
        Long totalTokens   = usageLogRepository.totalTokensForPeriod(companyId, target, target);
        Double avgMs       = usageLogRepository.avgResponseTimeMs(companyId, target);

        List<Object[]> byFeature = usageLogRepository.aggregateByFeature(
            companyId, target, target);

        Map<String, Long> requestsByFeature = new LinkedHashMap<>();
        Map<String, Long> tokensByFeature   = new LinkedHashMap<>();
        for (Object[] row : byFeature) {
            String key = row[0].toString();
            requestsByFeature.put(key, ((Number) row[1]).longValue());
            tokensByFeature.put(key,   ((Number) row[2]).longValue());
        }

        AiUsageSummaryResponse summary = new AiUsageSummaryResponse();
        summary.setDate(target);
        summary.setTotalRequests(totalRequests);
        summary.setTotalTokens(totalTokens != null ? totalTokens : 0L);
        summary.setAvgResponseTimeMs(avgMs != null ? avgMs : 0.0);
        summary.setRequestsByFeature(requestsByFeature);
        summary.setTokensByFeature(tokensByFeature);
        return summary;
    }

    @Override
    @Transactional
    public AiPromptTemplateResponse savePromptTemplate(AiPromptTemplateRequest request) {
        Long companyId   = securityUtil.getCurrentCompanyId();
        User currentUser = securityUtil.getCurrentUser();

        List<AiPromptTemplate> existing = (companyId != null)
            ? templateRepository.findByCompanyIdOrderByFeatureAscVersionDesc(companyId, Pageable.unpaged())
                .stream()
                .filter(t -> t.getFeature() == request.getFeature() && t.isActive())
                .collect(Collectors.toList())
            : templateRepository.findByCompanyIsNullOrderByFeatureAscVersionDesc(Pageable.unpaged())
                .stream()
                .filter(t -> t.getFeature() == request.getFeature() && t.isActive())
                .collect(Collectors.toList());

        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
        existing.forEach(t -> t.setActive(false));

        AiPromptTemplate template = AiPromptTemplate.builder()
            .feature(request.getFeature())
            .name(request.getName())
            .template(request.getTemplate())
            .version(nextVersion)
            .active(true)
            .changeNotes(request.getChangeNotes())
            .company(companyRef(companyId))
            .updatedBy(currentUser)
            .build();

        templateRepository.save(template);
        
        return AiMapper.toTemplateResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiPromptTemplateResponse> listPromptTemplates(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        Page<AiPromptTemplate> page = (companyId != null)
            ? templateRepository.findByCompanyIdOrderByFeatureAscVersionDesc(companyId, pageable)
            : templateRepository.findByCompanyIsNullOrderByFeatureAscVersionDesc(pageable);
        return page.map(AiMapper::toTemplateResponse);
    }

    @Override
    @Transactional
    public void deletePromptTemplate(Long id) {
        authorizationService.checkPermission(PermissionCode.AI_ADMIN);
        Long companyId = securityUtil.getCurrentCompanyId();
        AiPromptTemplate template = templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prompt template not found: " + id));
        Long templateCompanyId = template.getCompany() != null ? template.getCompany().getId() : null;
        if (!java.util.Objects.equals(templateCompanyId, companyId)) {
            throw new ResourceNotFoundException("Prompt template not found: " + id);
        }
        template.softDelete();
    }

    // ── Private helpers ───────────────────────────────────────────

    private static final int MAX_ATTEMPTS = 3; // 1 initial + 2 retries
    private static final long[] BACKOFF_MS = {300, 900};

    // Retries transient provider failures (timeouts, 429, 5xx) with a short backoff.
    // Non-retryable failures (bad request, auth, malformed response) fail immediately.
    private String generateWithRetry(AiProviderAdapter adapter, String prompt) {
        AiProviderException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return adapter.generate(prompt);
            } catch (AiProviderException e) {
                lastFailure = e;
                if (!e.isRetryable() || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                sleep(BACKOFF_MS[attempt - 1]);
            }
        }

        throw lastFailure;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void enforceRateLimits(Long companyId, Long userId) {
        long companyCount = usageLogRepository.countByCompanyAndDate(companyId, LocalDate.now());
        if (companyCount >= 200)
            throw new AiQuotaExceededException(
                "Daily AI request limit reached for this company (200/day). Try again tomorrow.");

        long userCount = usageLogRepository.countByUserAndDate(userId, LocalDate.now());
        if (userCount >= 50)
            throw new AiQuotaExceededException(
                "Daily AI request limit reached for this platformuser (50/day). Try again tomorrow.");
    }

    private String resolvePrompt(AiFeature feature, String callerPrompt, Long companyId) {
        List<AiPromptTemplate> templates =
            templateRepository.findActiveForFeature(feature, companyId);

        if (!templates.isEmpty()) {
            String tmpl = templates.get(0).getTemplate();
            return tmpl.contains("%s")
                ? tmpl.formatted(callerPrompt)
                : tmpl + "\n\nContext:\n" + callerPrompt;
        }

        return callerPrompt;
    }

    private Company companyRef(Long companyId) {
        if (companyId == null) return null;
        Company c = new Company();
        c.setId(companyId);
        return c;
    }
}
