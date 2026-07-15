package com.meta12.SS8911.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 결제 승인(confirm) API 호출 담당 서비스.
 * secret-key는 여기서만 사용하고 절대 프론트로 넘기지 않습니다.
 */
@Service
public class TossPaymentService {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.confirm-url}")
    private String confirmUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 토스 결제 최종 승인 요청.
     * 실패 시(금액 불일치, 이미 처리된 결제 등) RuntimeException을 던지므로
     * 호출부(Controller)에서 try-catch로 처리해야 합니다.
     */
    public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(confirmUrl, request, Map.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // 토스가 4xx로 거절한 경우 (금액 위변조, 이미 처리된 결제 등)
            throw new RuntimeException("결제 승인 거절: " + e.getResponseBodyAsString());
        }
    }
}