package com.businessos.modules.servicedesk.companyservice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.businessos.enums.SubscriptionStatus;

import java.util.List;

public interface ServicePackageService {

    // ── Package catalog (admin) ───────────────────────────────────

    ServicePackageResponse create(ServicePackageRequest request);

    ServicePackageResponse getById(Long id);

    Page<ServicePackageResponse> listAll(Pageable pageable);

    List<ServicePackageResponse> listActive();

    ServicePackageResponse update(Long id, ServicePackageRequest request);

    ServicePackageResponse toggleActive(Long id);

    void delete(Long id);

    // ── Subscriptions ────────────────────────────────────────────

    /** Client subscribes to a package (or admin subscribes on client's behalf). */
    PackageSubscriptionResponse subscribe(SubscribeRequest request);

    /** Admin activates a PENDING_PAYMENT subscription (simulates payment confirmation). */
    PackageSubscriptionResponse activate(Long subscriptionId);

    /** System entry point (payment gateway callbacks) - no security context. */
    PackageSubscriptionResponse activateForCompany(Long companyId, Long subscriptionId);

    /** Admin suspends an ACTIVE subscription. */
    PackageSubscriptionResponse suspend(Long subscriptionId, String reason);

    /** Admin or client cancels a subscription. */
    PackageSubscriptionResponse cancel(Long subscriptionId, String reason);

    /** Admin reactivates a SUSPENDED subscription. */
    PackageSubscriptionResponse reactivate(Long subscriptionId);

    /** Get one subscription by ID (tenant-scoped). */
    PackageSubscriptionResponse getSubscriptionById(Long id);

    /** List all subscriptions for this company (admin view). */
    Page<PackageSubscriptionResponse> listSubscriptions(
        SubscriptionStatus status, Pageable pageable);

    /** List subscriptions for the currently authenticated client. */
    Page<PackageSubscriptionResponse> listMySubscriptions(Pageable pageable);

    /**
     * Called by ServiceRequestServiceImpl when a client raises a request
     * under a subscription. Validates quota and increments requestsUsed.
     * Returns the updated subscription.
     */
    PackageSubscription consumeQuota(Long subscriptionId);
}
