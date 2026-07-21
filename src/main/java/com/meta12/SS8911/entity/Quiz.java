package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 연습퀴즈 "세트". 한 차시(Content)가 끝나면 나오는 문제 묶음 하나를 의미.
 * 실제 문항들은 QuizQuestion에 담김. 풀이 결과는 QuizBox에 기록됨.
 */
@Entity
@Table(name = "quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★ 이 문제 세트가 속한 강의(콘텐츠). "1강 연습퀴즈" 처럼 차시당 1세트.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false, length = 100)
    private String title; // 예: "1강 연습퀴즈"

    // ★ 문제은행 개념: 이 세트(예: 40문항) 중 매 시도마다 랜덤으로 몇 문항을 뽑아 출제할지.
    //   null이거나 전체 문항 수보다 크면 전체를 다 출제하는 걸로 처리.
    @Column(name = "question_count")
    private Integer questionCount;

    @Builder.Default
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> questions = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    public void addQuestion(QuizQuestion question) {
        questions.add(question);
        question.setQuiz(this);
    }

    @Builder.Default
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<QuizBox> quizBoxes = new ArrayList<>();
}