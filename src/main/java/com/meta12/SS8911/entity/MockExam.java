package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 실전 모의고사 "회차". 특정 테마(Category) 전체 범위를 다루는 시험 한 회.
 * 문제은행은 연습퀴즈와 완전히 별도 (MockExamQuestion).
 */
@Entity
@Table(name = "mock_exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★ 이 모의고사가 다루는 테마(케이팝/드라마/일상회화/심화 등)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title; // 예: "1회차 모의고사"

    // ★ 같은 테마 안에서의 회차 순서 (1, 2, 3...). 정렬에 사용.
    @Column(nullable = false)
    private Integer round;

    // ★ 제한시간(분) (기본 30분)
    @Column(name = "time_limit_minutes")
    @Builder.Default
    private Integer timeLimitMinutes = 30;

    @Builder.Default
    @OneToMany(mappedBy = "mockExam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockExamQuestion> questions = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    public void addQuestion(MockExamQuestion question) {
        questions.add(question);
        question.setMockExam(this);
    }
}