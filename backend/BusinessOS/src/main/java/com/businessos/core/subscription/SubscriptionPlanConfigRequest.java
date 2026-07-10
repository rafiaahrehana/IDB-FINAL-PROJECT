package com.businessos.core.subscription;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubscriptionPlanConfigRequest {
    @NotNull
    private SubscriptionPlan plan;

    @NotBlank
    private String displayName;

    private String description;

    @NotNull @DecimalMin("0")
    private BigDecimal price;

    private String currency;

    @Min(1)
    private int durationDays;

    private boolean featured;
    private boolean active;

    private int maxEmployees;
    private int maxClients;
    private int maxProjects;
    private int maxStorageMb;

    private boolean aiEnabled;
    private boolean websiteBuilderEnabled;
    private boolean customDomainEnabled;
    private boolean prioritySupport;
    private boolean apiAccess;

    private String features;
}
