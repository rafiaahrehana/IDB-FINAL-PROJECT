package com.businessos.modules.finance.invoice;

public class ClientInvoiceMapper {

    public static ClientInvoiceResponse toResponse(ClientInvoice entity) {
        if (entity == null) return null;

        return ClientInvoiceResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .invoiceNumber(entity.getInvoiceNumber())
                .clientName(entity.getClient() != null ? entity.getClient().getClientCompanyName() : null)
                .invoiceDate(entity.getInvoiceDate())
                .dueDate(entity.getDueDate())
                .subtotal(entity.getSubtotal())
                .taxAmount(entity.getTaxAmount())
                .totalAmount(entity.getTotalAmount())
                .paidAmount(entity.getPaidAmount())
                .balanceAmount(entity.getBalanceAmount())
                .status(entity.getStatus())
                .paymentTerms(entity.getPaymentTerms())
                .description(entity.getDescription())
                .notes(entity.getNotes())
                .sentDate(entity.getSentDate())
                .paidDate(entity.getPaidDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ClientInvoice toEntity(ClientInvoiceRequest request) {
        if (request == null) return null;

        return ClientInvoice.builder()
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .taxAmount(request.getTaxAmount())
                .paymentTerms(request.getPaymentTerms())
                .description(request.getDescription())
                .notes(request.getNotes())
                .build();
    }
}
