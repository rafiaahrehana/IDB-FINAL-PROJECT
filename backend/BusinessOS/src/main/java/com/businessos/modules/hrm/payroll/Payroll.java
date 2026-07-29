package com.businessos.modules.hrm.payroll;

import com.businessos.modules.hrm.employee.Employee;
import com.businessos.core.base.BaseEntity;

import com.businessos.modules.company.Company;
import com.businessos.enums.PayrollStatus;
import com.businessos.enums.PaymentMethod;
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
@Table(name = "payrolls",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month", "pay_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payroll extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "pay_month", nullable = false)
    private int payMonth;   // 1-12

    @Column(name = "pay_year", nullable = false)
    private int payYear;

    // Earnings
    @Builder.Default

    @Column(precision = 12, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Builder.Default


    @Column(precision = 12, scale = 2)
    private BigDecimal houseRent = BigDecimal.ZERO;

    @Builder.Default


    @Column(precision = 12, scale = 2)
    private BigDecimal medicalAllowance = BigDecimal.ZERO;

    @Builder.Default


    @Column(precision = 12, scale = 2)
    private BigDecimal transportAllowance = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal foodAllowance = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal specialAllowance = BigDecimal.ZERO;

    @Builder.Default


    @Column(precision = 12, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    // Billable pay: approved timesheet billableHours for this period * the employee's
    // billableRate, added to gross/net on top of the fixed salary components above.
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal billableHours = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal billableRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal billablePay = BigDecimal.ZERO;

    // Deductions
    @Builder.Default

    @Column(precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Builder.Default


    @Column(precision = 12, scale = 2)
    private BigDecimal taxDeduction = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal insuranceDeduction = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal providentFundDeduction = BigDecimal.ZERO;

    /**
     * Auto-calculated from attendance: (gross / calendar days in month) * unapproved
     * absent days for the period. Kept separate from the manual `deductions` field so
     * HR can see it wasn't hand-typed. Approved leave never counts as absent - see
     * AbsenteeMarkingService.
     */
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal attendanceDeduction = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "absent_days")
    private Integer absentDays = 0;

    // GL / Finance integration fields
    private String glDebitAccount;
    private String glCreditAccount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    // Net
    @Builder.Default

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String paymentReference;
    private LocalDate paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;
}
