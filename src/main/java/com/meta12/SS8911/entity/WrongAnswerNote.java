package com.meta12.SS8911.entity;

import com.meta12.SS8911.config.SourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 학습자가 퀴즈/모의고사에서 틀린 문제를 기록하는 오답노트.
 *
 * ★ 설계 포인트: 원본 문제(QuizQuestion, MockExamQuestion)를 FK로 직접 참조하지 않고
 *   틀렸을 당시의 문제/답/해설을 "스냅샷"으로 복사해서 저장함.
 *   이유:
 *   1) 연습퀴즈와 모의고사는 서로 다른 엔티티 트리라서 하나의 FK로 묶을 수 없음
 *   2) 원본 문제가 나중에 수정/삭제되어도 오답노트 내용은 그대로 보존됨
 *   sourceQuestionId는 참조용 정보일 뿐 실제 FK 제약조건은 걸지 않음.
 */
@Entity
@Getter
@Setter
public class WrongAnswerNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_user_id")
    private SiteUser siteUser;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType; // PRACTICE_QUIZ / MOCK_EXAM

    // 원본 문제 id (참조용 - FK 아님, 원본이 삭제돼도 이 값은 그대로 남음)
    private Long sourceQuestionId;

    // 문제/답/해설 스냅샷 (틀렸을 당시 그대로 보존)
    @Column(length = 1000)
    private String questionText;

    @Column(length = 500)
    private String userAnswer;

    @Column(length = 500)
    private String correctAnswer;

    @Column(length = 1000)
    private String explanation;

    // 어떤 퀴즈/모의고사 세트에서 틀렸는지 (통계/필터링용)
    private Long quizSetId;      // Quiz.id 또는 MockExam.id
    private String quizSetTitle; // 표시용 제목 스냅샷

    // 카테고리/단계 (통계 화면에서 분류용)
    private String category;

    private LocalDateTime createdDate;

    // 복습 완료 체크 여부 (학습자가 "이해했어요" 체크할 수 있게)
    private boolean reviewed;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
}