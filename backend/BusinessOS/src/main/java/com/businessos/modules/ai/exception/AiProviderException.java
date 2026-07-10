package com.businessos.modules.ai.exception;

import com.businessos.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AiProviderException extends ApiException {

    public AiProviderException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
