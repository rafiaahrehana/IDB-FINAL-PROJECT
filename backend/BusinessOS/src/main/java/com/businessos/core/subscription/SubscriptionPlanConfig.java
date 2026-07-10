package com.businessos.core.subscription;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plan_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionPlanConfig extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal price;

    @Builder.Default
    private String currency = "BDT";

    @Column(nullable = false)
    private int durationDays;

    @Builder.Default
    private boolean featured = false;

    @Builder.Default
    private boolean active = true;

    private int maxEmployees;
    private int maxClients;
    private int maxProjects;
    private int maxStorageMb;

    @Builder.Default
    private boolean aiEnabled = false;
    @Builder.Default
    private boolean websiteBuilderEnabled = false;
    @Builder.Default
    private boolean customDomainEnabled = false;
    @Builder.Default
    private boolean prioritySupport = false;
    @Builder.Default
    private boolean apiAccess = false;

    @Column(columnDefinition = "TEXT")
    private String features;
}
