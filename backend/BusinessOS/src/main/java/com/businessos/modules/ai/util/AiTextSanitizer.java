package com.businessos.modules.ai.util;

import org.springframework.stereotype.Component;


@Component
public class AiTextSanitizer {

    public String sanitize(String input) {
        if (input == null)
            return "";
        return input
            .replace("\\", "\\\\")
            .replace("\"", "'")
            .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
            .trim();
    }
}
