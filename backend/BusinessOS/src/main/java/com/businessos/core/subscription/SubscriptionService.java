package com.businessos.core.subscription;

import com.businessos.shared.payment.sslcommerz.SslCommerzInitResponse;

import java.util.List;

public interface SubscriptionService {
    List<PlanInfo> getPlans();
    SubscriptionResponse getCurrentSubscription();
    SslCommerzInitResponse checkout(String planName, String cusName, String cusEmail);
    void activateAfterPayment(String tranId);
    void cancelSubscription(Long id);
    void expireOverdueSubscriptions();
    void sendRenewalReminders();
    void sendExpiryReminders();

    record PlanInfo(String name, String description, int price, int durationDays, boolean featured) {}
}
