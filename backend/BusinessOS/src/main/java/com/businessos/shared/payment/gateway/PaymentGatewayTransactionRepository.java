package com.businessos.shared.payment.gateway;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentGatewayTransactionRepository
        extends JpaRepository<PaymentGatewayTransaction, Long> {

    Optional<PaymentGatewayTransaction> findByTranId(String tranId);
}
