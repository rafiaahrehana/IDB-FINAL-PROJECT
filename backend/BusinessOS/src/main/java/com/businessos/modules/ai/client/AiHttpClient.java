package com.businessos.modules.ai.client;

public interface AiHttpClient {

    String call(String prompt, String model, double temperature, int maxTokens);
}
