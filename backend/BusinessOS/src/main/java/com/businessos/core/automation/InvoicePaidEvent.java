package com.businessos.core.automation;

import lombok.Getter;

@Getter
public class InvoicePaidEvent extends BusinessEvent {

    private final Long invoiceId;
    private final Long clientId;
    private final java.math.BigDecimal amount;

    public InvoicePaidEvent(Object source, Long companyId,
                            Long invoiceId, Long clientId, java.math.BigDecimal amount) {
        super(source, companyId, "INVOICE_PAID");
        this.invoiceId = invoiceId;
        this.clientId = clientId;
        this.amount = amount;
    }
}
