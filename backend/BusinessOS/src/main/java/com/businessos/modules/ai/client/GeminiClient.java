package com.businessos.modules.ai.client;

import com.businessos.modules.ai.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor

public class GeminiClient implements AiHttpClient {

    private static final String BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    @Qualifier("aiRestTemplate")
    private final RestTemplate aiRestTemplate;

    @Setter
    private String apiKey;

    @Override
    public String call(String prompt, String model, double temperature, int maxTokens) {
        String url = String.format(BASE_URL, model, apiKey);

        Map<String, Object> body = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of(
                "temperature",     temperature,
                "maxOutputTokens", maxTokens
            )
        );

        try {
            Map<?, ?> response = aiRestTemplate.postForObject(url, body, Map.class);
            return extractText(response);
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProviderException("Gemini API call failed: " + e.getMessage());
        }
    }


    private String extractText(Map<?, ?> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> first    = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content  = (Map<?, ?>) first.get("content");
            List<?> parts      = (List<?>) content.get("parts");
            return (String) ((Map<?, ?>) parts.get(0)).get("text");
        } catch (Exception e) {
            throw new AiProviderException("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}
