package com.businessos.shared.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    Page<SubscriptionHistory> findByCompanyId(Long companyId, Pageable pageable);

    // All-time recorded subscription revenue (amountPaid is optional on a history row)
    @Query("SELECT COALESCE(SUM(h.amountPaid), 0) FROM SubscriptionHistory h")
    double sumTotalRevenue();

    // Revenue recorded since a given point in time (e.g. start of the current month)
    @Query("SELECT COALESCE(SUM(h.amountPaid), 0) FROM SubscriptionHistory h WHERE h.changedAt >= :from")
    double sumRevenueSince(@Param("from") LocalDateTime from);
}
