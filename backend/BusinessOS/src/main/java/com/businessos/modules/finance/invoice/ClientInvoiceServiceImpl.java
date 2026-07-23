package com.businessos.modules.finance.invoice;

import com.businessos.modules.company.Company;
import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.DefaultAccountResolver;
import com.businessos.modules.finance.generalledger.GeneralLedgerService;
import com.businessos.modules.finance.generalledger.GlReferenceType;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ForbiddenException;
import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.notification.CreateNotificationRequest;
import com.businessos.shared.notification.NotificationService;
import com.businessos.modules.servicedesk.servicerequest.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.businessos.enums.InvoiceStatus;
import com.businessos.enums.NotificationType;
import com.businessos.enums.RefundStatus;

@Service
@RequiredArgsConstructor
public class ClientInvoiceServiceImpl implements ClientInvoiceService {

    private final ClientInvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SecurityUtil securityUtil;
    private final GeneralLedgerService glService;
    private final DefaultAccountResolver accountResolver;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final AuthorizationService authorizationService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final RefundRepository refundRepository;
    private final NotificationService notificationService;
    private final InvoicePdfService invoicePdfService;

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
        authorizationService.checkPermission(PermissionCode.INVOICE_CREATE);
        return createInternal(securityUtil.getCurrentCompanyId(), request);
    }

    @Override
    @Transactional
    public ClientInvoiceResponse createForServiceRequest(Long companyId, ClientInvoiceRequest request) {
        return createInternal(companyId, request);
    }

    // Shared by the staff-facing create() (INVOICE_CREATE-gated) and
    // createForServiceRequest() - a client submitting their own paid service request
    // triggers this as a side effect of an action they're already authorized to take,
    // not a direct "create an invoice" call, so it must not require staff's INVOICE_CREATE.
    private ClientInvoiceResponse createInternal(Long companyId, ClientInvoiceRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        String invoiceNumber = generateInvoiceNumber(companyId);

        ClientInvoice invoice = ClientInvoice.builder()
                .companyId(companyId)
                .invoiceNumber(invoiceNumber)
                .client(client)
                .serviceRequest(request.getServiceRequestId() != null
                        ? serviceRequestRepository.getReferenceById(request.getServiceRequestId())
                        : null)
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
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        return ClientInvoiceMapper.toResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        ClientInvoice invoice = findInTenant(id);
        if (!authorizationService.hasPermission(PermissionCode.INVOICE_VIEW)) {
            requireOwnInvoice(invoice);
        }
        Company company = invoice.getClient() != null ? invoice.getClient().getCompany() : null;
        EmailBranding.Data branding = emailBranding.from(company);
        return invoicePdfService.generate(invoice, branding);
    }

    private void requireOwnInvoice(ClientInvoice invoice) {
        var currentUser = securityUtil.getCurrentUser();
        Client myClient = currentUser != null
                ? clientRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;
        if (myClient == null || invoice.getClient() == null
                || !invoice.getClient().getId().equals(myClient.getId())) {
            throw new ForbiddenException("Access denied: you can only download your own invoices");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClientInvoiceResponse getByInvoiceNumber(String number) {
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        ClientInvoice invoice = invoiceRepository.findByCompanyIdAndInvoiceNumber(requireCompanyId(), number)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return ClientInvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return invoiceRepository.findByCompanyId(companyId, pageable)
                .map(ClientInvoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getByStatus(InvoiceStatus status, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return invoiceRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(ClientInvoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getByClient(Long clientId, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        return findByClientUnchecked(clientId, pageable);
    }

    private Page<ClientInvoiceResponse> findByClientUnchecked(Long clientId, Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return invoiceRepository.findByCompanyIdAndClientId(companyId, clientId, pageable)
                .map(ClientInvoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientInvoiceResponse> getMyInvoices(Pageable pageable) {
        Long userId = securityUtil.getCurrentUser().getId();
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        Page<ClientInvoiceResponse> page = findByClientUnchecked(client.getId(), pageable);

        // Surface the latest refund's status (if any) so the client can see
        // "Refund Requested" / "Refund Rejected" without a separate call - a
        // processed refund is already visible via the invoice's own REFUNDED status.
        List<Long> invoiceIds = page.getContent().stream()
                .map(ClientInvoiceResponse::getId)
                .collect(Collectors.toList());
        if (!invoiceIds.isEmpty()) {
            Map<Long, RefundStatus> latestByInvoice = new HashMap<>();
            for (Refund r : refundRepository.findByClientInvoiceIdInOrderByCreatedAtDesc(invoiceIds)) {
                latestByInvoice.putIfAbsent(r.getClientInvoice().getId(), r.getStatus());
            }
            page.getContent().forEach(inv -> inv.setRefundStatus(latestByInvoice.get(inv.getId())));
        }

        return page;
    }

    @Override
    @Transactional
    public ClientInvoiceResponse update(Long id, ClientInvoiceRequest request) {
        authorizationService.checkPermission(PermissionCode.INVOICE_UPDATE);
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

        if (request.getItems() != null) {
            invoice.getItems().clear();
            final ClientInvoice invoiceRef = invoice;
            List<ClientInvoiceItem> items = request.getItems().stream()
                    .map(itemRequest -> {
                        ClientInvoiceItem item = ClientInvoiceItem.builder()
                                .invoice(invoiceRef)
                                .description(itemRequest.getDescription())
                                .quantity(itemRequest.getQuantity())
                                .unitPrice(itemRequest.getUnitPrice())
                                .notes(itemRequest.getNotes())
                                .build();
                        item.calculateLineTotal();
                        return item;
                    })
                    .collect(Collectors.toList());
            invoice.getItems().addAll(items);
        }

        invoice.calculateTotals();
        invoice = invoiceRepository.save(invoice);

        return ClientInvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public void sendInvoice(Long id) {
        authorizationService.checkPermission(PermissionCode.INVOICE_SEND);
        sendInvoiceInternal(id);
    }

    @Override
    @Transactional
    public void sendInvoiceForServiceRequest(Long id) {
        sendInvoiceInternal(id);
    }

    private void sendInvoiceInternal(Long id) {
        ClientInvoice invoice = findInTenant(id);  // tenant-scoped
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setSentDate(LocalDate.now());
        invoiceRepository.save(invoice);

        postInvoiceToLedger(invoice);

        // "Send Invoice" previously only flipped a status flag - the client was
        // never actually told an invoice existed.
        try {
            Client client = invoice.getClient();
            if (client != null && client.getUser() != null && client.getCompany() != null) {
                EmailBranding.Data branding = emailBranding.from(client.getCompany());
                emailService.sendInvoiceEmail(client.getUser().getEmail(), client.getUser().getFirstName(), branding);
            }
        } catch (Exception ex) {
            // Email failure must not roll back the status change and GL posting.
            // Log and continue - the invoice is already legally issued.
        }
    }

    /**
     * Revenue recognition: without this, invoices never touched the General Ledger at
     * all, so Profit & Loss / Balance Sheet showed no revenue even for fully paid
     * invoices - the two systems were completely disconnected.
     * Dr Accounts Receivable (full total) / Cr Sales Revenue (subtotal) + Cr Tax
     * Payable (tax, if any) - stays balanced since subtotal + tax = totalAmount.
     */
    private void postInvoiceToLedger(ClientInvoice invoice) {
        Long companyId = invoice.getCompanyId();
        String clientName = invoice.getClient() != null ? invoice.getClient().getClientCompanyName() : "client";
        String description = "Invoice " + invoice.getInvoiceNumber() + " issued to " + clientName;

        ChartOfAccount ar = accountResolver.accountsReceivable(companyId);
        glService.recordTransaction(companyId, ar.getId(), invoice.getTotalAmount(), BigDecimal.ZERO,
                description, GlReferenceType.INVOICE, invoice.getId(), invoice.getInvoiceNumber());

        ChartOfAccount revenue = accountResolver.salesRevenue(companyId);
        glService.recordTransaction(companyId, revenue.getId(), BigDecimal.ZERO, invoice.getSubtotal(),
                description, GlReferenceType.INVOICE, invoice.getId(), invoice.getInvoiceNumber());

        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            ChartOfAccount tax = accountResolver.taxPayable(companyId);
            glService.recordTransaction(companyId, tax.getId(), BigDecimal.ZERO, invoice.getTaxAmount(),
                    description, GlReferenceType.INVOICE, invoice.getId(), invoice.getInvoiceNumber());
        }
    }

    @Override
    @Transactional
    public void recordPayment(Long id, BigDecimal amount) {
        authorizationService.checkPermission(PermissionCode.INVOICE_PAYMENT);
        recordPaymentForCompany(requireCompanyId(), id, amount);
    }

    @Override
    @Transactional
    public void recordPaymentForCompany(Long companyId, Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.businessos.shared.exception.BadRequestException("Payment amount must be positive");
        }

        ClientInvoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
            .orElseThrow(() -> new com.businessos.shared.exception.ResourceNotFoundException("Invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new com.businessos.shared.exception.BadRequestException("Cannot record payment for a cancelled invoice");
        }

        BigDecimal outstanding = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (amount.compareTo(outstanding) > 0) {
            throw new com.businessos.shared.exception.BadRequestException("Payment amount exceeds outstanding balance");
        }

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

        // Cash received against a receivable: Dr Cash / Cr Accounts Receivable.
        String description = "Payment received for invoice " + invoice.getInvoiceNumber();
        ChartOfAccount cash = accountResolver.cash(companyId);
        glService.recordTransaction(companyId, cash.getId(), amount, BigDecimal.ZERO,
                description, GlReferenceType.INVOICE, invoice.getId(), invoice.getInvoiceNumber());
        ChartOfAccount ar = accountResolver.accountsReceivable(companyId);
        glService.recordTransaction(companyId, ar.getId(), BigDecimal.ZERO, amount,
                description, GlReferenceType.INVOICE, invoice.getId(), invoice.getInvoiceNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientInvoiceResponse> getOverdueInvoices() {
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        Long companyId = requireCompanyId();
        List<InvoiceStatus> paidStatuses = List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED);
        return invoiceRepository.findOverdueInvoices(companyId, paidStatuses)
                .stream()
                .map(ClientInvoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelInvoice(Long id) {
        authorizationService.checkPermission(PermissionCode.INVOICE_CANCEL);
        ClientInvoice invoice = findInTenant(id);

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new com.businessos.shared.exception.BadRequestException("Invoice is already cancelled");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new com.businessos.shared.exception.BadRequestException(
                    "Cannot cancel a fully paid invoice - issue a refund/credit note instead");
        }

        // DRAFT invoices were never posted to the ledger, so there's nothing to reverse.
        boolean wasPosted = invoice.getStatus() != InvoiceStatus.DRAFT;
        BigDecimal alreadyPaid = invoice.getPaidAmount();

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);

        if (wasPosted) {
            reverseInvoiceLedger(invoice, alreadyPaid);
        }
    }

    /**
     * Undoes exactly what postInvoiceToLedger() (and any recorded payments) posted:
     * Cr Accounts Receivable / Dr Sales Revenue + Dr Tax Payable for the full invoice,
     * and if any payment had already been received, Cr Cash / Dr Accounts Receivable
     * for that portion too - otherwise a cancelled invoice leaves permanently-wrong
     * AR/Revenue/Cash balances in the books.
     */
    private void reverseInvoiceLedger(ClientInvoice invoice, BigDecimal alreadyPaid) {
        reverseInvoiceLedger(invoice, alreadyPaid, GlReferenceType.INVOICE_CANCEL, "cancelled");
    }

    private void reverseInvoiceLedger(ClientInvoice invoice, BigDecimal alreadyPaid,
                                       GlReferenceType referenceType, String reasonWord) {
        Long companyId = invoice.getCompanyId();
        String description = "Invoice " + invoice.getInvoiceNumber() + " " + reasonWord + " - reversal";

        ChartOfAccount ar = accountResolver.accountsReceivable(companyId);
        glService.recordTransaction(companyId, ar.getId(), BigDecimal.ZERO, invoice.getTotalAmount(),
                description, referenceType, invoice.getId(), invoice.getInvoiceNumber());

        ChartOfAccount revenue = accountResolver.salesRevenue(companyId);
        glService.recordTransaction(companyId, revenue.getId(), invoice.getSubtotal(), BigDecimal.ZERO,
                description, referenceType, invoice.getId(), invoice.getInvoiceNumber());

        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            ChartOfAccount tax = accountResolver.taxPayable(companyId);
            glService.recordTransaction(companyId, tax.getId(), invoice.getTaxAmount(), BigDecimal.ZERO,
                    description, referenceType, invoice.getId(), invoice.getInvoiceNumber());
        }

        if (alreadyPaid != null && alreadyPaid.compareTo(BigDecimal.ZERO) > 0) {
            ChartOfAccount cash = accountResolver.cash(companyId);
            glService.recordTransaction(companyId, cash.getId(), BigDecimal.ZERO, alreadyPaid,
                    description, referenceType, invoice.getId(), invoice.getInvoiceNumber());
            glService.recordTransaction(companyId, ar.getId(), alreadyPaid, BigDecimal.ZERO,
                    description, referenceType, invoice.getId(), invoice.getInvoiceNumber());
        }
    }

    @Override
    @Transactional
    public void cancelOrRefundForServiceRequest(Long companyId, Long invoiceId) {
        ClientInvoice invoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            // Money already collected - needs staff review, not an instant reversal.
            if (refundRepository.existsByClientInvoiceIdAndStatus(invoiceId, RefundStatus.REQUESTED)) {
                return; // already has a pending refund request
            }
            Refund refund = Refund.builder()
                    .companyId(companyId)
                    .clientInvoice(invoice)
                    .requestedAmount(invoice.getPaidAmount())
                    .status(RefundStatus.REQUESTED)
                    .build();
            refundRepository.save(refund);
            return;
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.VOIDED
                || invoice.getStatus() == InvoiceStatus.REFUNDED) {
            return; // nothing to do
        }

        boolean wasPosted = invoice.getStatus() != InvoiceStatus.DRAFT;
        BigDecimal alreadyPaid = invoice.getPaidAmount();
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);
        if (wasPosted) {
            reverseInvoiceLedger(invoice, alreadyPaid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundResponse> listRefunds(RefundStatus status, Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.INVOICE_VIEW);
        Long companyId = requireCompanyId();
        Page<Refund> page = status != null
                ? refundRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                : refundRepository.findByCompanyId(companyId, pageable);
        return page.map(RefundMapper::toResponse);
    }

    @Override
    @Transactional
    public void processRefund(Long refundId) {
        authorizationService.checkPermission(PermissionCode.INVOICE_REFUND);
        Long companyId = requireCompanyId();
        Refund refund = refundRepository.findByIdAndCompanyId(refundId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found: " + refundId));

        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw new BadRequestException("Only a requested refund can be processed");
        }

        ClientInvoice invoice = refund.getClientInvoice();
        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is no longer in a paid state");
        }

        // Same reversal postInvoiceToLedger()/recordPaymentForCompany() posted, undone
        // here so the money genuinely leaves the company's books.
        reverseInvoiceLedger(invoice, invoice.getPaidAmount(), GlReferenceType.INVOICE_REFUND, "refunded");
        invoice.setStatus(InvoiceStatus.REFUNDED);
        invoiceRepository.save(invoice);

        refund.setStatus(RefundStatus.PROCESSED);
        refund.setProcessedBy(securityUtil.getCurrentUser());
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        notifyClientOfRefundDecision(invoice, NotificationType.REFUND_PROCESSED, "Refund Processed",
                "Your refund of " + refund.getRequestedAmount() + " for invoice "
                        + invoice.getInvoiceNumber() + " has been processed.");
    }

    @Override
    @Transactional
    public void rejectRefund(Long refundId, String reason) {
        authorizationService.checkPermission(PermissionCode.INVOICE_REFUND);
        Long companyId = requireCompanyId();
        Refund refund = refundRepository.findByIdAndCompanyId(refundId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found: " + refundId));

        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw new BadRequestException("Only a requested refund can be rejected");
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setRejectionReason(reason);
        refundRepository.save(refund);

        notifyClientOfRefundDecision(refund.getClientInvoice(), NotificationType.REFUND_REJECTED, "Refund Rejected",
                "Your refund request for invoice " + refund.getClientInvoice().getInvoiceNumber()
                        + " was rejected." + (reason != null && !reason.isBlank() ? " Reason: " + reason : ""));
    }

    private void notifyClientOfRefundDecision(ClientInvoice invoice, NotificationType type, String title, String message) {
        try {
            Client client = invoice.getClient();
            if (client != null && client.getUser() != null) {
                notificationService.send(CreateNotificationRequest.of(
                        type, title, message, "/client/payments", client.getUser().getId(), invoice.getCompanyId()));
            }
        } catch (Exception ex) {
            // Notification failure must not roll back the refund decision itself.
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.INVOICE_DELETE);
        ClientInvoice invoice = findInTenant(id);  // tenant-scoped
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new com.businessos.shared.exception.BadRequestException(
                    "Only DRAFT invoices can be deleted - use cancel for an already-sent invoice");
        }
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