package com.businessos.core.subscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionCheckoutRequest {
    @NotBlank(message = "Plan is required")
    private String plan;

    @NotBlank(message = "Name is required")
    private String cusName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String cusEmail;
}
