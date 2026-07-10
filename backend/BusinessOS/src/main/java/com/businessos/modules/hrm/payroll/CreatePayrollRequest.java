package com.businessos.modules.hrm.payroll;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePayrollRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    @NotNull
    @Min(1)
    @Max(12)
    private Integer payMonth;
    @NotNull
    @Min(2020)
    private Integer payYear;
    @NotNull
    private BigDecimal basicSalary;
    private BigDecimal houseRent;
    private BigDecimal medicalAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal taxDeduction;
    private String notes;
}
