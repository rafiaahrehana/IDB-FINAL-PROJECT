package com.businessos.modules.finance.payment;

import com.businessos.modules.crm.client.Client;
import com.businessos.modules.crm.client.ClientRepository;
import com.businessos.modules.finance.invoice.ClientInvoice;
import com.businessos.modules.finance.invoice.ClientInvoiceRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentReceiptServiceImpl implements PaymentReceiptService {

    private final PaymentReceiptRepository receiptRepository;
    private final ClientInvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public PaymentReceiptResponse create(PaymentReceiptRequest request) {
        Long companyId = securityUtil.getCurrentCompanyId();

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        ClientInvoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getInvoiceId())
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
        PaymentReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment receipt not found"));
        return PaymentReceiptMapper.toResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentReceiptResponse> getAll(Pageable pageable) {
        Long companyId = securityUtil.getCurrentCompanyId();
        return receiptRepository.findByCompanyId(companyId, pageable)
                .map(PaymentReceiptMapper::toResponse);
    }

    @Override
    @Transactional
    public void confirmPayment(Long id) {
        PaymentReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment receipt not found"));
        receipt.confirmPayment();
        receiptRepository.save(receipt);
    }

    @Override
    @Transactional
    public void markAsDeposited(Long id, String bank) {
        PaymentReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment receipt not found"));
        receipt.markAsDeposited(bank);
        receiptRepository.save(receipt);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PaymentReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment receipt not found"));
        receipt.softDelete();
        receiptRepository.save(receipt);
    }

    private String generateReceiptNumber(Long companyId) {
        long count = receiptRepository.count() + 1;
        return String.format("RCP-%04d-%06d", LocalDate.now().getYear(), count);
    }
}
