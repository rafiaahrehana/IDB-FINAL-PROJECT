package com.businessos.modules.ai.provider;

import com.businessos.modules.ai.enums.AiModel;
import com.businessos.modules.ai.enums.AiProviderType;

/**
 * Business abstraction over one AI provider.
 * Wraps an AiHttpClient with resolved credentials and model config.
 * AiProviderResolver builds the correct adapter at runtime.
 */
public interface AiProviderAdapter {

    String generate(String prompt);

    AiProviderType getProviderType();

    AiModel getModel();
}
