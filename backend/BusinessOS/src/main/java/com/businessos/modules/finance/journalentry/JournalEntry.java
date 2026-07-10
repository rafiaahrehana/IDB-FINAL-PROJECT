package com.businessos.modules.finance.journalentry;

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
@Table(name = "journal_entries", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "journal_entry_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry extends BaseEntity {

    private Long companyId; // Tenant isolation

    @Column(name = "journal_entry_number", nullable = false)
    private String journalEntryNumber; // JE-2024-001

    private LocalDate entryDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debit_account_id", nullable = false)
    private ChartOfAccount debitAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_account_id", nullable = false)
    private ChartOfAccount creditAccount;

    private BigDecimal amount;

    private String description;
    private String notes;

    private String createdBy; // User who created
    private LocalDate createdDate;

    private String approvedBy; // User who approved
    private LocalDate approvedDate;

    @Builder.Default
    private boolean approved = false;

    @Builder.Default
    private boolean posted = false;

    private LocalDate postedDate;

    public void approve(String approverName) {
        this.approved = true;
        this.approvedBy = approverName;
        this.approvedDate = LocalDate.now();
    }

    public void post() {
        if (!approved) {
            throw new IllegalStateException("Journal entry must be approved before posting");
        }
        this.posted = true;
        this.postedDate = LocalDate.now();
    }
}
