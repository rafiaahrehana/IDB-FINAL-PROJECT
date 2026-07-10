package com.businessos.core.subscription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private String plan;
    private BigDecimal amount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String paymentMethod;
    private int durationMonths;
    private boolean autoRenew;
}
