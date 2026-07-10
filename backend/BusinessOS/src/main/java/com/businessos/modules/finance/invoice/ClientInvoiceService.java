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
    ClientInvoiceResponse update(Long id, ClientInvoiceRequest request);
    void sendInvoice(Long id);
    void recordPayment(Long id, java.math.BigDecimal amount);
    void markAsOverdue(Long id);
    List<ClientInvoiceResponse> getOverdueInvoices();
    void delete(Long id);
}
