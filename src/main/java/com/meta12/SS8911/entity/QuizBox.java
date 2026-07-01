package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class QuizBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser user;

    private int score;           // 맞은 개수
    private int total;           // 전체 문항 수
    private LocalDateTime solvedDate; // 푼 날짜
}
