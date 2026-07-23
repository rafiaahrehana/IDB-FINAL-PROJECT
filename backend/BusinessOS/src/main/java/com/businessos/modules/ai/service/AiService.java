package com.businessos.modules.ai.service;

import com.businessos.modules.ai.dto.request.AiGenerateRequest;
import com.businessos.modules.ai.dto.request.AiPromptTemplateRequest;
import com.businessos.modules.ai.dto.request.AiProviderConfigRequest;
import com.businessos.modules.ai.dto.response.AiGenerateResponse;

import com.businessos.modules.ai.dto.response.AiPromptTemplateResponse;
import com.businessos.modules.ai.dto.response.AiProviderConfigResponse;
import com.businessos.modules.ai.dto.response.AiUsageSummaryResponse;
import com.businessos.modules.ai.enums.AiFeature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AiService {

    /** Generate text for a given feature using the company's resolved provider */
    AiGenerateResponse generate(AiGenerateRequest request);

    /**
     * Internal use by HRM, CRM, Finance — takes a pre-built prompt string
     */
    String generateFromPrompt(AiFeature feature, String prompt);

    /** OWNER / ADMIN: configure a custom AI provider for the company */
    AiProviderConfigResponse saveProviderConfig(AiProviderConfigRequest request);

    /** OWNER / ADMIN: get the company's active provider config */
    AiProviderConfigResponse getProviderConfig();

    /** OWNER / ADMIN: every provider the company has saved (one per provider type, at most one active) */
    List<AiProviderConfigResponse> listProviderConfigs();

    /** OWNER / ADMIN: switch which saved config is used for generation - deactivates all others */
    AiProviderConfigResponse activateProviderConfig(Long id);

    /** OWNER / ADMIN: remove a saved config - the active one can't be deleted, activate another first */
    void deleteProviderConfig(Long id);

    /** OWNER / ADMIN: list conversation history */
    Page<AiGenerateResponse> listConversations(AiFeature feature, Pageable pageable);

    /** OWNER / ADMIN: get usage summary for a date */
    AiUsageSummaryResponse getUsageSummary(LocalDate date);

    /** OWNER / ADMIN: save or update a prompt template for a feature */
    AiPromptTemplateResponse savePromptTemplate(AiPromptTemplateRequest request);

    /** OWNER / ADMIN: list prompt templates for the company */
    Page<AiPromptTemplateResponse> listPromptTemplates(Pageable pageable);
}
