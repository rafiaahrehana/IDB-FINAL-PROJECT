package com.businessos.modules.finance.invoice;

import java.math.BigDecimal;

public class ClientInvoiceItemMapper {
    public static ClientInvoiceItem toEntity(ClientInvoiceItemRequest request) {
        if (request == null) return null;
        
        ClientInvoiceItem item = ClientInvoiceItem.builder()
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .notes(request.getNotes())
                .build();
        item.calculateLineTotal();
        return item;
    }
}
