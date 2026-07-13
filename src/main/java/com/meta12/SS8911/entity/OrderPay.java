package com.meta12.SS8911.entity;


import com.meta12.SS8911.config.OrderPayStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderPay {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id; // 기본키

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        private Category category; // 카테고리 엔티티와 연결

        private String price; // 결제금액
        private LocalDateTime payday; // 결제날짜
        private String cardNumber; // 카드번호
        private String payType; // 결제수단 (ex: 카드)
        private String instructorName; // 강사이름
        private String planType; // 구독 플랜 (월구독 / 연구독 / 평생) - 개별 강좌 구매는 null

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "siteUser_id")
        private SiteUser siteUser; // 사용자 엔티티와 연결

        // --- [토스 페이먼츠 연동을 위한 추가 필드] ---

        private String orderId; // 결제 요청 시 생성한 고유 주문번호

        private String paymentKey; // 토스 승인 완료 후 발급되는 결제 고유 키 (취소/조회 시 필수)

        @Enumerated(EnumType.STRING)
        private OrderPayStatus status; // 결제 상태 (SUCCESS, CANCEL, FAILED)

}