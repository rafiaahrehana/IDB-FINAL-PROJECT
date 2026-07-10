package com.businessos.shared.payment.sslcommerz;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sslcommerz")
@RequiredArgsConstructor
public class SslCommerzController {

    private final SslCommerzService sslCommerzService;

    @PostMapping("/init")
    public ResponseEntity<SslCommerzInitResponse> initPayment(@Valid @RequestBody SslCommerzInitRequest request) {
        return ResponseEntity.ok(sslCommerzService.initPayment(request));
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> handleIpn(@RequestBody Map<String, String> params) {
        sslCommerzService.handleIpn(params);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/status/{tranId}")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable String tranId) {
        String status = sslCommerzService.validateAndGetStatus(tranId);
        return ResponseEntity.ok(Map.of("tranId", tranId, "status", status));
    }
}
