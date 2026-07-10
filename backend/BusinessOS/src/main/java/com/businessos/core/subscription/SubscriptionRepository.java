package com.businessos.core.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByCompanyIdAndStatusOrderByEndDateDesc(Long companyId, Subscription.SubscriptionStatus status);

    List<Subscription> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate <= :cutoffDate AND s.endDate >= :today AND (s.reminderSent IS NULL OR s.reminderSent = false)")
    List<Subscription> findActiveNeedingRenewalReminder(LocalDate today, LocalDate cutoffDate);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :today")
    List<Subscription> findExpiredSubscriptions(LocalDate today);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate <= :cutoffDate AND s.endDate >= :today AND (s.expiryReminderSent IS NULL OR s.expiryReminderSent = false)")
    List<Subscription> findActiveNeedingExpiryReminder(LocalDate today, LocalDate cutoffDate);

    Optional<Subscription> findByTranId(String tranId);
}
