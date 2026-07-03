package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 학습자에게 보여줄 개별 문항 DTO.
 * ★ answer(정답 번호)는 절대 포함하지 않음 — 채점은 서버(QuizService)에서만 처리.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDTO {

    private Long id;
    private String question;
    private String questionType; // ★ 배지 표시용 (예: 종합 대화형)
    private List<String> options; // [option1, option2, option3, option4] 순서 그대로

    public static QuizQuestionDTO from(QuizQuestion q) {
        return QuizQuestionDTO.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .questionType(q.getQuestionType())
                .options(List.of(q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()))
                .build();
    }
}