package com.businessos.core.subscription;

import com.businessos.shared.payment.sslcommerz.SslCommerzInitResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/company/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionService.PlanInfo>> getPlans() {
        return ResponseEntity.ok(subscriptionService.getPlans());
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription() {
        SubscriptionResponse sub = subscriptionService.getCurrentSubscription();
        return ResponseEntity.ok(sub != null ? sub : Map.of("status", "NONE", "plan", "FREE"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<SslCommerzInitResponse> checkout(@Valid @RequestBody SubscriptionCheckoutRequest request) {
        return ResponseEntity.ok(subscriptionService.checkout(request.getPlan(), request.getCusName(), request.getCusEmail()));
    }

    @PostMapping("/activate/{tranId}")
    public ResponseEntity<Map<String, String>> activate(@PathVariable String tranId) {
        subscriptionService.activateAfterPayment(tranId);
        return ResponseEntity.ok(Map.of("status", "activated", "tranId", tranId));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(Map.of("status", "cancelled"));
    }
}
