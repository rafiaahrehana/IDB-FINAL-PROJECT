package com.businessos.core.subscription;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubscriptionPlanConfigResponse {
    private Long id;
    private String plan;
    private String displayName;
    private String description;
    private BigDecimal price;
    private String currency;
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
