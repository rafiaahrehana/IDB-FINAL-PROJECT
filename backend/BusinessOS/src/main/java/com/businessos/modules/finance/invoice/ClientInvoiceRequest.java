package com.businessos.modules.finance.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientInvoiceRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotEmpty(message = "Invoice must have at least one item")
    private List<ClientInvoiceItemRequest> items;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    private PaymentTerms paymentTerms;
    private String description;
    private String notes;
}