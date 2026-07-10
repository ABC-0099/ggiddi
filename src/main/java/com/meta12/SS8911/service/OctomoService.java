package com.meta12.SS8911.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OctomoService {

    @Value("${octomo.api-key}")
    private String apiKey;

    private static final String EXISTS_URL = "https://api.octoverse.kr/octomo/v1/public/message/exists";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 해당 번호(mobileNum)가 인증 코드(code)를 포함한 문자를
     * 옥토모 대표번호로 보냈는지 확인합니다.
     */
    public boolean checkMessageExists(String mobileNum, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Octomo " + apiKey);

        Map<String, String> body = Map.of(
                "mobileNum", normalize(mobileNum),
                "text", code
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(EXISTS_URL, request, Map.class);
            Object exists = response.getBody() != null ? response.getBody().get("exists") : null;
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 010-1234-5678 → 01012345678 형태로 정규화 (하이픈/공백 제거)
    private String normalize(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }
}