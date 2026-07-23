package com.businessos.modules.finance.invoice;

import com.businessos.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Page<Refund> findByCompanyIdAndStatus(Long companyId, RefundStatus status, Pageable pageable);

    Page<Refund> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Refund> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByClientInvoiceIdAndStatus(Long clientInvoiceId, RefundStatus status);

    // Ordered newest-first so the caller can take the first match per invoice as "latest".
    List<Refund> findByClientInvoiceIdInOrderByCreatedAtDesc(List<Long> clientInvoiceIds);
}
