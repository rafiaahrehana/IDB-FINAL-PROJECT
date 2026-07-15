package com.businessos.modules.finance.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.businessos.enums.InvoiceStatus;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInvoiceResponse {
    private Long id;
    private Long companyId;
    private String invoiceNumber;
    private Long clientId;
    private String clientName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private List<ClientInvoiceItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private InvoiceStatus status;
    private PaymentTerms paymentTerms;
    private String description;
    private String notes;
    private LocalDate sentDate;
    private LocalDate paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
