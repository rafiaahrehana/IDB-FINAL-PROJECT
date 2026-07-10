package com.businessos.shared.payment.sslcommerz;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SslCommerzInitRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.00", message = "Minimum amount is 10 BDT")
    private BigDecimal amount;

    private String currency = "BDT";

    @NotBlank(message = "Customer name is required")
    private String cusName;

    @NotBlank(message = "Customer email is required")
    private String cusEmail;

    private String cusPhone = "";
    private String cusAdd1 = "Dhaka";
    private String cusCity = "Dhaka";
    private String cusCountry = "Bangladesh";
}
