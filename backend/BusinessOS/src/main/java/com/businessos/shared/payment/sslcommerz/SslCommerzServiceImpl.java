package com.businessos.shared.payment.sslcommerz;

import com.businessos.enums.WalletTransactionType;
import com.businessos.shared.payment.wallet.WalletService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SslCommerzServiceImpl implements SslCommerzService {

    private final SslCommerzPaymentRepository paymentRepository;
    private final WalletService walletService;
    private final SecurityUtil securityUtil;

    @Value("${sslcommerz.store-id:testbox}")
    private String storeId;

    @Value("${sslcommerz.store-password:qwerty}")
    private String storePassword;

    @Value("${sslcommerz.sandbox:true}")
    private boolean sandbox;

    @Value("${sslcommerz.sandbox-url:https://sandbox.sslcommerz.com}")
    private String sandboxUrl;

    @Value("${sslcommerz.live-url:https://securepay.sslcommerz.com}")
    private String liveUrl;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    @Transactional
    public SslCommerzInitResponse initPayment(SslCommerzInitRequest request) {
        Long companyId = requireCompanyId();
        String tranId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String baseUrl = sandbox ? sandboxUrl : liveUrl;

        Map<String, String> params = new LinkedHashMap<>();
        params.put("store_id", storeId);
        params.put("store_passwd", storePassword);
        params.put("total_amount", request.getAmount().toPlainString());
        params.put("currency", request.getCurrency());
        params.put("tran_id", tranId);
        params.put("product_category", "wallet_topup");
        params.put("product_profile", "non-physical-goods");
        params.put("product_name", "Wallet Top-Up");

        // Callback URLs
        params.put("success_url", frontendUrl + "/finance/sslcommerz/success?tran_id=" + tranId);
        params.put("fail_url", frontendUrl + "/finance/sslcommerz/fail?tran_id=" + tranId);
        params.put("cancel_url", frontendUrl + "/finance/sslcommerz/cancel?tran_id=" + tranId);
        params.put("ipn_url", "http://localhost:8085/api/sslcommerz/ipn");

        // Customer info
        params.put("cus_name", request.getCusName());
        params.put("cus_email", request.getCusEmail());
        params.put("cus_phone", request.getCusPhone());
        params.put("cus_add1", request.getCusAdd1());
        params.put("cus_city", request.getCusCity());
        params.put("cus_country", request.getCusCountry());
        params.put("cus_postcode", "1000");
        params.put("cus_state", "Dhaka");

        // Shipping
        params.put("ship_name", request.getCusName());
        params.put("ship_add1", request.getCusAdd1());
        params.put("ship_city", request.getCusCity());
        params.put("ship_state", "Dhaka");
        params.put("ship_postcode", "1000");
        params.put("ship_country", request.getCusCountry());
        params.put("shipping_method", "NO");

        // Custom fields
        params.put("value_a", String.valueOf(companyId));

        // Save payment record
        SslCommerzPayment payment = SslCommerzPayment.builder()
                .tranId(tranId)
                .companyId(companyId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("INITIATED")
                .initiatedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Call SSLCommerz API
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder body = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (body.length() > 0) body.append("&");
                body.append(entry.getKey()).append("=").append(entry.getValue());
            }

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/gwprocess/v4/api.php",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> resp = response.getBody();
            if (resp != null && "SUCCESS".equals(resp.get("status"))) {
                payment.setSessionKey((String) resp.get("sessionkey"));
                payment.setGatewayPageUrl((String) resp.get("GatewayPageURL"));
                paymentRepository.save(payment);

                return new SslCommerzInitResponse(
                        true,
                        (String) resp.get("GatewayPageURL"),
                        (String) resp.get("sessionkey"),
                        tranId,
                        "Payment session created"
                );
            }

            return new SslCommerzInitResponse(false, null, null, tranId,
                    resp != null ? (String) resp.get("failedreason") : "Failed to connect to SSLCommerz");

        } catch (Exception e) {
            log.error("SSLCommerz init error", e);
            return new SslCommerzInitResponse(false, null, null, tranId, "Failed to connect to SSLCommerz: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public SslCommerzInitResponse initPaymentForSubscription(SslCommerzInitRequest request, Long companyId, String planName, int durationDays) {
        String tranId = "SUB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String baseUrl = sandbox ? sandboxUrl : liveUrl;

        Map<String, String> params = new LinkedHashMap<>();
        params.put("store_id", storeId);
        params.put("store_passwd", storePassword);
        params.put("total_amount", request.getAmount().toPlainString());
        params.put("currency", request.getCurrency());
        params.put("tran_id", tranId);
        params.put("product_category", "subscription");
        params.put("product_profile", "non-physical-goods");
        params.put("product_name", "BusinessOS " + planName + " Plan");

        params.put("success_url", frontendUrl + "/company/subscription/success?tran_id=" + tranId);
        params.put("fail_url", frontendUrl + "/company/subscription/fail?tran_id=" + tranId);
        params.put("cancel_url", frontendUrl + "/company/subscription/cancel?tran_id=" + tranId);
        params.put("ipn_url", "http://localhost:8085/api/sslcommerz/ipn");

        params.put("cus_name", request.getCusName());
        params.put("cus_email", request.getCusEmail());
        params.put("cus_phone", request.getCusPhone() != null ? request.getCusPhone() : "");
        params.put("cus_add1", request.getCusAdd1() != null ? request.getCusAdd1() : "Dhaka");
        params.put("cus_city", request.getCusCity() != null ? request.getCusCity() : "Dhaka");
        params.put("cus_country", request.getCusCountry() != null ? request.getCusCountry() : "Bangladesh");
        params.put("cus_postcode", "1000");
        params.put("cus_state", "Dhaka");

        params.put("ship_name", request.getCusName());
        params.put("ship_add1", "Dhaka");
        params.put("ship_city", "Dhaka");
        params.put("ship_state", "Dhaka");
        params.put("ship_postcode", "1000");
        params.put("ship_country", "Bangladesh");
        params.put("shipping_method", "NO");

        params.put("value_a", "SUB_" + companyId + "_" + planName + "_" + durationDays);

        SslCommerzPayment payment = SslCommerzPayment.builder()
                .tranId(tranId)
                .companyId(companyId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("INITIATED")
                .initiatedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder body = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (body.length() > 0) body.append("&");
                body.append(entry.getKey()).append("=").append(entry.getValue());
            }

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/gwprocess/v4/api.php",
                    HttpMethod.POST, entity, Map.class);

            Map<String, Object> resp = response.getBody();
            if (resp != null && "SUCCESS".equals(resp.get("status"))) {
                payment.setSessionKey((String) resp.get("sessionkey"));
                payment.setGatewayPageUrl((String) resp.get("GatewayPageURL"));
                paymentRepository.save(payment);
                return new SslCommerzInitResponse(true,
                        (String) resp.get("GatewayPageURL"),
                        (String) resp.get("sessionkey"),
                        tranId, "Payment session created");
            }

            return new SslCommerzInitResponse(false, null, null, tranId,
                    resp != null ? (String) resp.get("failedreason") : "Failed to connect to SSLCommerz");

        } catch (Exception e) {
            log.error("SSLCommerz subscription init error", e);
            return new SslCommerzInitResponse(false, null, null, tranId, "Failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleIpn(Map<String, String> params) {
        String tranId = params.get("tran_id");
        String status = params.get("status");

        if (tranId == null) return;

        SslCommerzPayment payment = paymentRepository.findByTranId(tranId)
                .orElse(null);
        if (payment == null || !"INITIATED".equals(payment.getStatus())) return;

        payment.setValId(params.get("val_id"));
        payment.setBankTranId(params.get("bank_tran_id"));
        payment.setCardType(params.get("card_type"));
        payment.setCardNo(params.get("card_no"));
        payment.setCardIssuer(params.get("card_issuer"));
        payment.setCardBrand(params.get("card_brand"));
        payment.setRiskLevel(params.get("risk_level"));
        payment.setRiskTitle(params.get("risk_title"));

        if ("VALID".equals(status)) {
            payment.setStatus("VALID");
            payment.setValidatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Check if this is a subscription payment (tranId starts with SUB-)
            if (tranId.startsWith("SUB-")) {
                activateSubscriptionFromIpn(tranId, payment);
            } else {
                // Credit wallet
                walletService.credit("COMPANY", payment.getCompanyId(), payment.getAmount(),
                        WalletTransactionType.CREDIT, "SSLCommerz-" + tranId,
                        "Online payment via SSLCommerz (" + payment.getCardType() + ")");
            }
        } else {
            payment.setStatus(status);
            payment.setValidatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }

    @Override
    @Transactional
    public String validateAndGetStatus(String tranId) {
        SslCommerzPayment payment = paymentRepository.findByTranId(tranId)
                .orElse(null);
        if (payment == null) return "NOT_FOUND";

        if ("VALID".equals(payment.getStatus())) return "VALID";

        String baseUrl = sandbox ? sandboxUrl : liveUrl;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = baseUrl + "/validator/api/validationserverAPI.php"
                    + "?val_id=" + (payment.getValId() != null ? payment.getValId() : "")
                    + "&store_id=" + storeId
                    + "&store_passwd=" + storePassword
                    + "&format=json";

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> resp = response.getBody();

            if (resp != null) {
                String respStatus = (String) resp.get("status");
                payment.setStatus(respStatus);
                payment.setValId((String) resp.get("val_id"));
                payment.setBankTranId((String) resp.get("bank_tran_id"));
                payment.setValidatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                if ("VALID".equals(respStatus) || "VALIDATED".equals(respStatus)) {
                    walletService.credit("COMPANY", payment.getCompanyId(), payment.getAmount(),
                            WalletTransactionType.CREDIT, "SSLCommerz-" + tranId,
                            "Online payment via SSLCommerz");
                    return "VALID";
                }
            }

            return payment.getStatus();
        } catch (Exception e) {
            log.error("SSLCommerz validation error", e);
            return payment.getStatus();
        }
    }

    private void activateSubscriptionFromIpn(String tranId, SslCommerzPayment payment) {
        log.info("Subscription payment validated for tranId: {}. Activation will occur on redirect.", tranId);
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }
}
