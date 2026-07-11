package com.businessos.modules.ai.client;


import com.businessos.modules.ai.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor

public class ClaudeClient implements AiHttpClient {

    private static final String BASE_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Qualifier("aiRestTemplate")
    private final RestTemplate aiRestTemplate;

    @Setter
    private String apiKey;

    @Override
    public String call(String prompt, String model, double temperature, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        Map<String, Object> body = Map.of(
            "model",      model,
            "max_tokens", maxTokens,
            "temperature", temperature,
            "messages",   List.of(Map.of("role", "platformuser", "content", prompt))
        );

        try {
            ResponseEntity<Map> response = aiRestTemplate.exchange(
                BASE_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            return extractText(response.getBody());
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProviderException("Claude API call failed: " + e.getMessage(), isRetryable(e));
        }
    }

    private String extractText(Map<?, ?> response) {
        try {
            List<?> content = (List<?>) response.get("content");
            return (String) ((Map<?, ?>) content.get(0)).get("text");
        } catch (Exception e) {
            throw new AiProviderException("Failed to parse Claude response: " + e.getMessage());
        }
    }

    private boolean isRetryable(Exception e) {
        if (e instanceof HttpClientErrorException client) {
            return client.getStatusCode().value() == 429;
        }
        return true;
    }
}
