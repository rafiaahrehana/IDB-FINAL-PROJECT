package com.businessos.modules.finance.invoice;

import com.businessos.core.base.BaseEntity;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.businessos.enums.InvoiceStatus;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "companyId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Entity
@Table(name = "client_invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "invoice_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientInvoice extends BaseEntity {

    private Long companyId; // Tenant isolation

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber; // INV-2024-001

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientInvoiceItem> items;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentTerms paymentTerms;

    private String description;
    private String notes;

    private LocalDate sentDate;
    private LocalDate paidDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    public void calculateTotals() {
        if (items != null) {
            subtotal = items.stream()
                    .map(ClientInvoiceItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        totalAmount = subtotal.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        balanceAmount = totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) &&
                !InvoiceStatus.PAID.equals(status);
    }
}
