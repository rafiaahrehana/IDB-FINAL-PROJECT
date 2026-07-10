package com.businessos.shared.payment.sslcommerz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class SslCommerzInitResponse {
    private boolean success;
    private String gatewayPageUrl;
    private String sessionKey;
    private String tranId;
    private String message;
}
