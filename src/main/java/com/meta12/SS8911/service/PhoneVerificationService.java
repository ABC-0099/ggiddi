package com.meta12.SS8911.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final OctomoService octomoService;

    // 서버가 한 대라서(공유 개발 환경) 메모리 저장으로 충분합니다.
    // 전화번호(숫자만) → 인증 상태
    private final ConcurrentHashMap<String, VerificationEntry> store = new ConcurrentHashMap<>();

    // 옥토모 자체 메시지 조회 윈도우(최근 5분)와 맞춤
    private static final long EXPIRE_MINUTES = 5;

    /** 인증코드를 새로 발급하고 저장합니다. */
    public String issueCode(String phone) {
        String normalized = normalize(phone);
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000)); // 4자리
        store.put(normalized, new VerificationEntry(code, LocalDateTime.now(), false));
        return code;
    }

    /** 옥토모 API로 실제 문자 수신 여부를 확인하고, 확인되면 인증 완료 처리합니다. */
    public boolean confirm(String phone) {
        String normalized = normalize(phone);
        VerificationEntry entry = store.get(normalized);
        if (entry == null) return false;

        if (entry.issuedAt.plusMinutes(EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            store.remove(normalized);
            return false; // 만료됨 - 재발급 필요
        }

        boolean exists = octomoService.checkMessageExists(normalized, entry.code);
        if (exists) {
            entry.verified = true;
        }
        return exists;
    }

    /** 회원가입 최종 제출 시, 해당 번호가 인증 완료 상태인지 확인합니다. */
    public boolean isVerified(String phone) {
        VerificationEntry entry = store.get(normalize(phone));
        return entry != null && entry.verified;
    }

    /** 가입 완료 후 임시 데이터 정리용(선택). */
    public void clear(String phone) {
        store.remove(normalize(phone));
    }

    private String normalize(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }

    private static class VerificationEntry {
        final String code;
        final LocalDateTime issuedAt;
        boolean verified;

        VerificationEntry(String code, LocalDateTime issuedAt, boolean verified) {
            this.code = code;
            this.issuedAt = issuedAt;
            this.verified = verified;
        }
    }
}