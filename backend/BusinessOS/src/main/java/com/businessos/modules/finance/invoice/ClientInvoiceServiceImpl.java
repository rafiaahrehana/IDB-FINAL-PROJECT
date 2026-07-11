package com.businessos.modules.finance.invoice;

import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.businessos.enums.InvoiceStatus;

@Service
@RequiredArgsConstructor
public class ClientInvoiceServiceImpl implements ClientInvoiceService {

    private final ClientInvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SecurityUtil securityUtil;

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new com.businessos.shared.exception.BadRequestException("No company context");
        return id;
    }

    private ClientInvoice findInTenant(Long id) {
        return invoiceRepository.findByIdAndCompanyId(id, requireCompanyId())
            .orElseThrow(() -> new com.businessos.shared.exception.ResourceNotFoundException("Invoice not found: " + id));
    }

    @Override
    @Transactional
    public ClientInvoiceResponse create(ClientInvoiceRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        String invoiceNumber = generateInvoiceNumber(companyId);

        ClientInvoice invoice = ClientInvoice.builder()
                .companyId(companyId)
                .invoiceNumber(invoiceNumber)
                .client(client)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .paymentTerms(request.getPaymentTerms())
                .description(request.getDescription())
                .notes(request.getNotes())
                .status(InvoiceStatus.DRAFT)
                .build();

        // Add items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<ClientInvoiceItem> items = request.getItems().stream()
                    .map(itemRequest -> {
                        ClientInvoiceItem item = ClientInvoiceItem.builder()
                                .invoice(invoice)
                                .description(itemRequest.getDescription())
                                .quantity(itemRequest.getQuantity())
                                .unitPrice(itemRequest.getUnitPrice())
                                .notes(itemRequest.getNotes())
                                .build();
                        item.calculateLineTotal();
                        return item;
                    })
                    .collect(Collectors.toList());
            invoice.setItems(items);
        }

        invoice.calculateTotals();
        ClientInvoice savedInvoice = invoiceRepository.save(invoice);

        return ClientInvoiceMapper.toResponse(savedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientInvoiceResponse getById(Long id) {
        return ClientInvoiceMapper.toResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientInvoiceResponse getByInvoiceNumber(String number) {
        ClientInvoice invoice = invoiceRepository.findByInvoiceNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return ClientInvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return invoiceRepository.findByCompanyId(companyId, pageable)
                .map(ClientInvoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getByStatus(InvoiceStatus status, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return invoiceRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(ClientInvoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getByClient(Long clientId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return invoiceRepository.findByCompanyIdAndClientId(companyId, clientId, pageable)
                .map(ClientInvoiceMapper::toResponse);
    }

    @Override
    @Transactional
    public ClientInvoiceResponse update(Long id, ClientInvoiceRequest request) {
        ClientInvoice invoice = findInTenant(id);  // tenant-scoped

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new com.businessos.shared.exception.BadRequestException("Only DRAFT invoices can be updated");
        }

        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setPaymentTerms(request.getPaymentTerms());
        invoice.setDescription(request.getDescription());
        invoice.setNotes(request.getNotes());

        invoice.calculateTotals();
        invoice = invoiceRepository.save(invoice);

        return ClientInvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public void sendInvoice(Long id) {
        ClientInvoice invoice = findInTenant(id);  // tenant-scoped
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setSentDate(LocalDate.now());
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void recordPayment(Long id, BigDecimal amount) {
        recordPaymentForCompany(requireCompanyId(), id, amount);
    }

    @Override
    @Transactional
    public void recordPaymentForCompany(Long companyId, Long id, BigDecimal amount) {
        ClientInvoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
            .orElseThrow(() -> new com.businessos.shared.exception.ResourceNotFoundException("Invoice not found: " + id));

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidDate(LocalDate.now());
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoice.calculateTotals();
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientInvoiceResponse> getOverdueInvoices() {
        Long companyId = requireCompanyId();
        List<InvoiceStatus> paidStatuses = List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED);
        return invoiceRepository.findOverdueInvoices(companyId, paidStatuses)
                .stream()
                .map(ClientInvoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ClientInvoice invoice = findInTenant(id);  // tenant-scoped
        invoice.softDelete();
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void markAsOverdue(Long id) {
        ClientInvoice invoice = findInTenant(id);
        if (invoice.getStatus() != InvoiceStatus.PAID && invoice.getStatus() != InvoiceStatus.CANCELLED) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
        }
    }

    /**
     * Generates a unique, per-company, per-year invoice number.
     * Format: INV-YYYY-NNNNNN (e.g., INV-2026-000042)
     *
     * Uses MAX on existing numbers instead of COUNT to be safe against
     * concurrent inserts and soft-deleted records skewing the count.
     */
    private String generateInvoiceNumber(Long companyId) {
        int year = LocalDate.now().getYear();
        String prefix = "INV-" + year + "-";
        String maxNumber = invoiceRepository
            .findMaxInvoiceNumberByCompanyAndPrefix(companyId, prefix)
            .orElse(prefix + "000000");
        long sequence = Long.parseLong(maxNumber.substring(prefix.length())) + 1;
        return String.format("%s%06d", prefix, sequence);
    }
}