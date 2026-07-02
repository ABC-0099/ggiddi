package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 결제(구독) 페이지의 플랜 카드 하나를 표현하는 DTO.
 * list.html의 .plan-card 를 서버 데이터로 렌더링하기 위해 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {

    private String tag;             // 월구독 / 연구독 / 평생 (식별키, JS에서도 이 값으로 조회)
    private String name;            // 월 구독 / 연 구독 / 평생 소장 ✨
    private int price;              // 실 결제가
    private int originalPrice;      // 정가
    private int discount;           // 할인액 (원가 - 실결제가)
    private String perLabel;        // "/ 매월 자동 갱신" 같은 주기 설명
    private String saveLabel;       // "월 7,492원 · 25% 절약" (없으면 null)
    private boolean popular;        // 인기 배지 + 기본 선택 여부
    private String buttonLabel;     // "구독 시작하기" / "결제하기"
    private List<String> benefits;  // 오른쪽 주문요약 박스에 표시되는 혜택 3줄
    private List<Feature> features; // 카드 안의 체크리스트

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feature {
        private String label;
        private boolean included; // true면 체크(✓), false면 회색 처리(off)
    }

}
