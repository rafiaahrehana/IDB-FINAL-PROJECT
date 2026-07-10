package com.businessos.modules.ai.util;

import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Decrypts API keys stored in ai_provider_configs.
 * Development: Base64 decode only.
 * Production: replace with AES-256-GCM using a Vault-managed key.
 */
@Component
public class AiKeyDecryptor {

    public String decrypt(String encryptedKey) {
        if (encryptedKey == null || encryptedKey.isBlank())
            throw new IllegalArgumentException("Cannot decrypt a null or blank API key");
        return new String(Base64.getDecoder().decode(encryptedKey));
    }

    public String encrypt(String plainKey) {
        if (plainKey == null || plainKey.isBlank())
            throw new IllegalArgumentException("Cannot encrypt a null or blank API key");
        return Base64.getEncoder().encodeToString(plainKey.getBytes());
    }
}
