package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate; // 날짜만 저장하는 것이 가장 관리하기 편합니다

@Entity
@Getter
@Setter

public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 출석한 날짜
    private LocalDate date;

    // 누가 출석했는지 (사용자와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SiteUser siteUser;
}