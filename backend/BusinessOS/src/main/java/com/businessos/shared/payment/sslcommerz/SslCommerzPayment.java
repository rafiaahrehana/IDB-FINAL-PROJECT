package com.businessos.shared.payment.sslcommerz;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sslcommerz_payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SslCommerzPayment extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String tranId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "BDT";

    @Column(nullable = false)
    private String status; // INITIATED, VALID, FAILED, CANCELLED

    private String sessionKey;
    private String valId;
    private String bankTranId;
    private String cardType;
    private String cardNo;
    private String cardIssuer;
    private String cardBrand;

    private String riskLevel;
    private String riskTitle;

    private String gatewayPageUrl;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    private LocalDateTime validatedAt;
}
