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

public class OpenAiClient implements AiHttpClient {

    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";

    @Qualifier("aiRestTemplate")
    private final RestTemplate aiRestTemplate;

    @Setter
    private String apiKey;

    @Override
    public String call(String prompt, String model, double temperature, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "model",       model,
            "temperature", temperature,
            "max_tokens",  maxTokens,
            "messages",    List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            ResponseEntity<Map> response = aiRestTemplate.exchange(
                BASE_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            return extractText(response.getBody());
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProviderException("OpenAI API call failed: " + e.getMessage(), isRetryable(e));
        }
    }


    private String extractText(Map<?, ?> response) {
        try {
            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new AiProviderException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }

    private boolean isRetryable(Exception e) {
        if (e instanceof HttpClientErrorException client) {
            return client.getStatusCode().value() == 429;
        }
        return true;
    }
}
