package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mock_exam_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mock_exam_id", nullable = false)
    private MockExam mockExam;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(length = 30)
    private String questionType; // 종합 대화형 / 어휘 / 문법 등 (연습퀴즈와 동일한 태그 재사용)

    @Column(nullable = false, length = 200)
    private String option1;

    @Column(nullable = false, length = 200)
    private String option2;

    @Column(nullable = false, length = 200)
    private String option3;

    @Column(nullable = false, length = 200)
    private String option4;

    @Column(nullable = false)
    private Integer answer;

    @Column(length = 500)
    private String explanation;
}