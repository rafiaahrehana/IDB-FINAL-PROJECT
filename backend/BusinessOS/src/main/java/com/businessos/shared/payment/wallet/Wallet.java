package com.businessos.shared.payment.wallet;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// @FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
// @Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet extends BaseEntity {

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // Promotional / trial credit balance — separate from real cash balance.
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal creditBalance = BigDecimal.ZERO;

    @Builder.Default
    private String currency = "BDT";

    @Column(name = "context_type", nullable = false)
    private String contextType; // PLATFORM, COMPANY, CLIENT

    @Column(name = "context_id", nullable = false)
    private Long contextId;

    // Optional constraint: UNIQUE(context_type, context_id)
    // Total spendable amount = cash balance + credit balance.
    public BigDecimal getTotalAvailable() {
        BigDecimal b = balance      != null ? balance      : BigDecimal.ZERO;
        BigDecimal c = creditBalance != null ? creditBalance : BigDecimal.ZERO;
        return b.add(c);
    }
}
