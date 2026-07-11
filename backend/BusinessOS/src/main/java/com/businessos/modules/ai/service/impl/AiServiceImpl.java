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

    @Override
    @Transactional
    public AiGenerateResponse generate(AiGenerateRequest request) {
        User user      = securityUtil.getCurrentUser();
        Long companyId = requireCompanyId();
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
    public AiProviderConfigResponse saveProviderConfig(AiProviderConfigRequest request) {
        Long companyId = requireCompanyId();

        AiProviderConfig config = configRepository.findByCompanyIdAndActiveTrue(companyId)
            .orElseGet(() -> { AiProviderConfig c = new AiProviderConfig(); c.setCompany(companyRef(companyId)); return c; });

        config.setAiProviderType(request.getAiProviderType());
        config.setAiModel(request.getModel());
        config.setActive(true);

        if (request.getApiKey() != null && !request.getApiKey().isBlank())
            config.setApiKeyEncrypted(keyDecryptor.encrypt(request.getApiKey()));
        if (request.getTemperature() != null)
            config.setTemperature(request.getTemperature());
        if (request.getMaxTokens() != null)
            config.setMaxTokens(request.getMaxTokens());

        configRepository.save(config);
        
        return AiMapper.toConfigResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public AiProviderConfigResponse getProviderConfig() {
        return configRepository.findByCompanyIdAndActiveTrue(requireCompanyId())
            .map(AiMapper::toConfigResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No AI provider config found for this company"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiGenerateResponse> listConversations(AiFeature feature, Pageable pageable) {
        Long companyId = requireCompanyId();

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
        Long companyId   = requireCompanyId();
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
        Long companyId   = requireCompanyId();
        User currentUser = securityUtil.getCurrentUser();

        List<AiPromptTemplate> existing = templateRepository
            .findByCompanyIdOrderByFeatureAscVersionDesc(companyId, Pageable.unpaged())
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
        return templateRepository
            .findByCompanyIdOrderByFeatureAscVersionDesc(requireCompanyId(), pageable)
            .map(AiMapper::toTemplateResponse);
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

    private Long requireCompanyId() {
        Long companyId = securityUtil.getCurrentCompanyId();
        if (companyId == null)
            throw new ResourceNotFoundException("No company context in security context");
        return companyId;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company();
        c.setId(companyId);
        return c;
    }
}
