package com.businessos.modules.finance.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentReceiptService {
    PaymentReceiptResponse create(PaymentReceiptRequest request);
    PaymentReceiptResponse getById(Long id);
    Page<PaymentReceiptResponse> getAll(Pageable pageable);
    void confirmPayment(Long id);
    void markAsDeposited(Long id, String bank);
    void delete(Long id);
}
