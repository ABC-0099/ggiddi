package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDTO {

    private Long quizId;
    private int score;   // 맞은 개수
    private int total;   // 전체 문항 수
    private List<QuestionResult> questionResults;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private Long questionId;
        private boolean correct;
        private Integer correctAnswer; // 오답이었을 경우에만 정답 번호 공개
        private String explanation;
    }
}