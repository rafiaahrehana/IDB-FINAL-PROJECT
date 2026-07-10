package com.businessos.core.subscription;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription extends BaseEntity {

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "BDT";

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private String tranId;
    @Builder.Default
    private String paymentMethod = "SSLCOMMERZ";

    @Builder.Default
    private int durationMonths = 1;

    @Builder.Default
    private boolean autoRenew = false;

    private Boolean reminderSent;
    private Boolean expiryReminderSent;

    private LocalDateTime createdAt;

    public enum SubscriptionStatus {
        ACTIVE, EXPIRED, CANCELLED, PENDING, SUSPENDED
    }
}
