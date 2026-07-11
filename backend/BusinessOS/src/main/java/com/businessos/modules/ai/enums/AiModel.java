package com.businessos.modules.ai.enums;

public enum AiModel {

    GEMINI_2_5_FLASH("gemini-2.5-flash"),
    GEMINI_2_5_PRO("gemini-2.5-pro"),
    GPT_4O("gpt-4o"),
    GPT_4O_MINI("gpt-4o-mini"),
    CLAUDE_SONNET("claude-sonnet-4-6"),
    CLAUDE_OPUS("claude-opus-4-6"),
    GROQ_LLAMA_3_3_70B("llama-3.3-70b-versatile"),
    GROQ_LLAMA_3_1_8B("llama-3.1-8b-instant");

    private final String modelId;

    AiModel(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}
