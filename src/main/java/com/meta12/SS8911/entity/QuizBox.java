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

    // ★ true면 강의 시청 페이지(content/view)에 임베드된 퀴즈, false면 배움터(/quiz) 연습퀴즈.
    //   임베드 퀴즈는 랜덤으로 일부 문항만 뽑아 따로 진행되는 것이라, 연습퀴즈 통계/최근점수에는 포함시키지 않음.
    private boolean embedded;
}