package com.businessos.modules.ai.dto.request;

import com.businessos.modules.ai.enums.AiFeature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AiGenerateRequest {

    @NotNull(message = "Feature is required")
    private AiFeature feature;

    @NotBlank(message = "Prompt is required")
    private String prompt;

    public AiFeature getFeature() { return feature; }
    public void setFeature(AiFeature feature) { this.feature = feature; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}
