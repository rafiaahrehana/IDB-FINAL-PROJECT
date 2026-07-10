package com.businessos.modules.finance.reconciliation;

import com.businessos.core.base.BaseEntity;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.math.BigDecimal;
import java.time.LocalDate;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "bank_reconciliations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankReconciliation extends BaseEntity {

    private Long companyId; // Tenant isolation

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private ChartOfAccount bankAccount;

    private LocalDate reconciliationDate;

    private BigDecimal glBalance;
    private BigDecimal bankStatementBalance;
    private BigDecimal difference;

    private String outstandingDeposits; // JSON list
    private String outstandingChecks; // JSON list

    @Builder.Default
    private boolean reconciled = false;

    private String discrepancyNotes;
    private LocalDate reconciledDate;
    private String reconciledBy;

    public void markAsReconciled(String reconciledByName) {
        this.reconciled = true;
        this.reconciledDate = LocalDate.now();
        this.reconciledBy = reconciledByName;
    }

    public boolean hasDiscrepancy() {
        return difference != null && difference.compareTo(BigDecimal.ZERO) != 0;
    }
}