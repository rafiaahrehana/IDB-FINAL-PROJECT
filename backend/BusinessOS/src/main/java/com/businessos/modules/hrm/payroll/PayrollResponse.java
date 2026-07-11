package com.businessos.modules.hrm.payroll;

import com.businessos.enums.PayrollStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PayrollResponse {
    private Long id;
    private int payMonth;
    private int payYear;
    private BigDecimal basicSalary;
    private BigDecimal houseRent;
    private BigDecimal medicalAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal taxDeduction;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private String paymentReference;
    private LocalDate paidAt;
    private String notes;
    private Long employeeId;
    private String employeeName;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
}
