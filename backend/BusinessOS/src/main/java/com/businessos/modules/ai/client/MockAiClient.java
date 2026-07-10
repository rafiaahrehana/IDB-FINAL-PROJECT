package com.businessos.modules.ai.client;


import org.springframework.stereotype.Component;
@Component

public class MockAiClient implements AiHttpClient {

    @Override
    public String call(String prompt, String model, double temperature, int maxTokens) {
        
        return "[MOCK AI RESPONSE] Prompt received ("
            + prompt.length() + " chars). "
            + "Configure ai.default-provider=gemini to use a real provider.";
    }
}
