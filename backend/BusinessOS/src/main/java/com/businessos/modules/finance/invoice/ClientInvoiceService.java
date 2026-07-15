package com.businessos.modules.finance.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.businessos.enums.InvoiceStatus;

public interface ClientInvoiceService {

    ClientInvoiceResponse create(ClientInvoiceRequest request);
    ClientInvoiceResponse getById(Long id);
    ClientInvoiceResponse getByInvoiceNumber(String number);
    Page<ClientInvoiceResponse> getAll(Pageable pageable);
    Page<ClientInvoiceResponse> getByStatus(InvoiceStatus status, Pageable pageable);
    Page<ClientInvoiceResponse> getByClient(Long clientId, Pageable pageable);

    /** The caller's own invoices - resolves their Client record from the security context. */
    Page<ClientInvoiceResponse> getMyInvoices(Pageable pageable);
    ClientInvoiceResponse update(Long id, ClientInvoiceRequest request);
    void sendInvoice(Long id);
    void recordPayment(Long id, java.math.BigDecimal amount);

    /** System entry point (e.g. payment gateway callbacks) - no security context. */
    void recordPaymentForCompany(Long companyId, Long id, java.math.BigDecimal amount);
    void markAsOverdue(Long id);
    List<ClientInvoiceResponse> getOverdueInvoices();

    /** Reverses any GL posting the invoice already made, then marks it CANCELLED. */
    void cancelInvoice(Long id);

    /** DRAFT only - anything already sent/posted must go through cancelInvoice(). */
    void delete(Long id);
}
