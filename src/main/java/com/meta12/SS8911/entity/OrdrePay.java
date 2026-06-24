package com.meta12.SS8911.entity;


import com.meta12.SS8911.config.OrderPayStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OrdrePay {
    //결제
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne // 카테고리 엔티티와 연결
//    @JoinColumn(name = "category_id")
//    private Category category;

    private Long price; //결제금액

    private LocalDateTime payday; // 결제날짜

    private String cardNumber; // 카드번호

    private String payType;//결제수단 (ex 카드 )
    private String instructorName;//강사이름
    private String planType; // 월별 연별 결제 구분용
    @Enumerated(EnumType.STRING)
    private OrderPayStatus status; //결제 성공 취고 이넘으로
//    @ManyToOne
//    @JoinColumn(name = "siteUser_id") // DB에서 어떤 컬럼과 연결할지 명시
//    private SiteUser siteUser;

}
