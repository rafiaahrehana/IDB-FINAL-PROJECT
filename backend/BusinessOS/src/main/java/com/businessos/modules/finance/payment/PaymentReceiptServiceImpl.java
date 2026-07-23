package com.businessos.modules.finance.payment;

import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.finance.chartofaccounts.ChartOfAccount;
import com.businessos.modules.finance.chartofaccounts.DefaultAccountResolver;
import com.businessos.modules.finance.generalledger.GeneralLedgerService;
import com.businessos.modules.finance.generalledger.GlReferenceType;
import com.businessos.modules.finance.invoice.ClientInvoice;
import com.businessos.modules.finance.invoice.ClientInvoiceRepository;
import com.businessos.modules.finance.invoice.ClientInvoiceService;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentReceiptServiceImpl implements PaymentReceiptService {

    private final PaymentReceiptRepository receiptRepository;
    private final ClientInvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SecurityUtil securityUtil;
    private final ClientInvoiceService clientInvoiceService;
    private final GeneralLedgerService glService;
    private final DefaultAccountResolver accountResolver;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public PaymentReceiptResponse create(PaymentReceiptRequest request) {
        authorizationService.checkPermission(PermissionCode.PAYMENT_RECEIPT_CREATE);
        Long companyId = securityUtil.getCurrentCompanyId();

        Client client = clientRepository.findByIdAndCompanyId(request.getClientId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        ClientInvoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findByIdAndCompanyId(request.getInvoiceId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        }

        String receiptNumber = generateReceiptNumber(companyId);

        PaymentReceipt receipt = PaymentReceipt.builder()
                .companyId(companyId)
                .receiptNumber(receiptNumber)
                .invoice(invoice)
                .client(client)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .status(PaymentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        receipt = receiptRepository.save(receipt);
        return PaymentReceiptMapper.toResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentReceiptResponse getById(Long id) {
        authorizationService.checkPermission(PermissionCode.PAYMENT_RECEIPT_VIEW);
        return PaymentReceiptMapper.toResponse(findInTenant(id));
    }

    private PaymentReceipt findInTenant(Long id) {
        return receiptRepository.findByIdAndCompanyId(id, securityUtil.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment receipt not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentReceiptResponse> getAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.PAYMENT_RECEIPT_VIEW);
        Long companyId = securityUtil.getCurrentCompanyId();
        return receiptRepository.findByCompanyId(companyId, pageable)
                .map(PaymentReceiptMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentReceiptResponse> getMyReceipts(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        Long userId = securityUtil.getCurrentUser().getId();
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
        return receiptRepository.findByCompanyIdAndClientId(companyId, client.getId(), pageable)
                .map(PaymentReceiptMapper::toResponse);
    }

    @Override
    @Transactional
    public void confirmPayment(Long id) {
        authorizationService.checkPermission(PermissionCode.PAYMENT_RECEIPT_CONFIRM);
        PaymentReceipt receipt = findInTenant(id);
        if (receipt.getStatus() == PaymentStatus.CONFIRMED || receipt.getStatus() == PaymentStatus.DEPOSITED) {
            throw new BadRequestException("Payment receipt is already confirmed");
        }
        receipt.confirmPayment();
        receiptRepository.save(receipt);

        // Previously confirming a receipt only flipped its own status - it never
        // touched the invoice it was recorded against (paidAmount/status stayed
        // stale) and never hit the General Ledger, so invoices could sit "ISSUED"
        // forever even after being fully paid, and revenue/cash never showed in
        // Finance reports.
        if (receipt.getInvoice() != null) {
            ClientInvoice invoice = invoiceRepository.findByIdAndCompanyId(receipt.getInvoice().getId(), receipt.getCompanyId()).orElse(null);
            if (invoice != null && invoice.getStatus() != com.businessos.enums.InvoiceStatus.CANCELLED) {
                clientInvoiceService.recordPaymentForCompany(receipt.getCompanyId(), receipt.getInvoice().getId(), receipt.getAmount());
            }
        } else {
            String description = "Payment received (receipt " + receipt.getReceiptNumber() + ")";
            ChartOfAccount cash = accountResolver.cash(receipt.getCompanyId());
            glService.recordTransaction(cash.getId(), receipt.getAmount(), BigDecimal.ZERO,
                    description, GlReferenceType.PAYMENT_RECEIPT, receipt.getId(), receipt.getReceiptNumber());
            ChartOfAccount ar = accountResolver.accountsReceivable(receipt.getCompanyId());
            glService.recordTransaction(ar.getId(), BigDecimal.ZERO, receipt.getAmount(),
                    description, GlReferenceType.PAYMENT_RECEIPT, receipt.getId(), receipt.getReceiptNumber());
        }
    }

    @Override
    @Transactional
    public void markAsDeposited(Long id, String bank) {
        authorizationService.checkPermission(PermissionCode.PAYMENT_RECEIPT_CONFIRM);
        PaymentReceipt receipt = findInTenant(id);
        receipt.markAsDeposited(bank);
        receiptRepository.save(receipt);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.PAYMENT_RECEIPT_DELETE);
        PaymentReceipt receipt = findInTenant(id);
        receipt.softDelete();
        receiptRepository.save(receipt);
    }

    private String generateReceiptNumber(Long companyId) {
        int year = LocalDate.now().getYear();
        String prefix = "RCP-" + year + "-";
        String maxNumber = receiptRepository
                .findMaxReceiptNumberByCompanyAndPrefix(companyId, prefix)
                .orElse(prefix + "000000");
        long sequence = Long.parseLong(maxNumber.substring(prefix.length())) + 1;
        return String.format("%s%06d", prefix, sequence);
    }
}
