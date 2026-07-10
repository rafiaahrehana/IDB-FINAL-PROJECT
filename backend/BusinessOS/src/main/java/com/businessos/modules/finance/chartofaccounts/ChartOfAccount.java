package com.businessos.modules.finance.chartofaccounts;

import com.businessos.core.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "chart_of_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "account_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartOfAccount extends BaseEntity {

    private Long companyId; // Tenant isolation

    @Column(nullable = false)
    private String accountCode; // e.g., "1000", "5100"

    private String accountName; // e.g., "Cash", "Salary Expense"

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private AccountType type; // ASSET, LIABILITY, REVENUE, EXPENSE, etc.

    private String description; // Detailed explanation

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO; // Current balance

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean isHeaderAccount = false; // Parent account for grouping

    // For sub-accounts
    private Long parentAccountId; // If this is a sub-account

    // Configuration
    @Builder.Default
    private boolean allowDirectPosting = true; // Can JE be posted directly?

    private String notes;

    public BigDecimal getDebitBalance() {
        if (type == AccountType.ASSET || type == AccountType.CONTRA_LIABILITY ||
                type == AccountType.EXPENSE || type == AccountType.CONTRA_REVENUE) {
            return balance;
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getCreditBalance() {
        if (type == AccountType.LIABILITY || type == AccountType.CONTRA_ASSET ||
                type == AccountType.EQUITY || type == AccountType.REVENUE) {
            return balance;
        }
        return BigDecimal.ZERO;
    }
}