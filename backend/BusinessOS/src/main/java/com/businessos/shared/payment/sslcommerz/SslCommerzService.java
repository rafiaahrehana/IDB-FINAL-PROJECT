package com.businessos.shared.payment.sslcommerz;

import java.util.Map;

public interface SslCommerzService {
    SslCommerzInitResponse initPayment(SslCommerzInitRequest request);
    SslCommerzInitResponse initPaymentForSubscription(SslCommerzInitRequest request, Long companyId, String planName, int durationDays);
    void handleIpn(Map<String, String> params);
    String validateAndGetStatus(String tranId);
}
