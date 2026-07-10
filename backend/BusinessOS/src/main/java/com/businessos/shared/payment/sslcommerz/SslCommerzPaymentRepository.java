package com.businessos.shared.payment.sslcommerz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SslCommerzPaymentRepository extends JpaRepository<SslCommerzPayment, Long> {
    Optional<SslCommerzPayment> findByTranId(String tranId);
    Page<SslCommerzPayment> findByCompanyIdOrderByInitiatedAtDesc(Long companyId, Pageable pageable);
}
