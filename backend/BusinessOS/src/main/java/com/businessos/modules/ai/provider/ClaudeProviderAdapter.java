package com.businessos.modules.ai.provider;

import com.businessos.modules.ai.client.ClaudeClient;
import com.businessos.modules.ai.enums.AiModel;
import com.businessos.modules.ai.enums.AiProviderType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClaudeProviderAdapter implements AiProviderAdapter {

    private final ClaudeClient client;
    private final AiModel model;
    private final double temperature;
    private final int maxTokens;

    @Override
    public String generate(String prompt) {
        return client.call(prompt, model.getModelId(), temperature, maxTokens);
    }

    @Override
    public AiProviderType getProviderType() {
        return AiProviderType.CLAUDE;
    }

    @Override
    public AiModel getModel() {
        return model;
    }
}
