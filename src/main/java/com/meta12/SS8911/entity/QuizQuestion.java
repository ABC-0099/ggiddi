package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Quiz(문제 세트) 안에 속한 개별 문항. 4지선다 단일 정답.
 */
@Entity
@Table(name = "quiz_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 500)
    private String question;

    // ★ 문제 유형 태그 (예: 종합 대화형, 어휘, 문법, 표현, 발음). 관리자가 등록 시 수동 선택.
    @Column(length = 30)
    private String questionType;

    @Column(nullable = false, length = 200)
    private String option1;

    @Column(nullable = false, length = 200)
    private String option2;

    @Column(nullable = false, length = 200)
    private String option3;

    @Column(nullable = false, length = 200)
    private String option4;

    // ★ 정답 번호 1~4 (option1~4 중 몇 번이 정답인지)
    @Column(nullable = false)
    private Integer answer;

    @Column(length = 500)
    private String explanation; // 정답 해설 (선택)
}