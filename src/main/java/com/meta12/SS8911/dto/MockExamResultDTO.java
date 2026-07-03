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
public class MockExamResultDTO {

    private Long examId;
    private int score;
    private int total;

    @Builder.Default
    private List<QuestionResult> questionResults = new java.util.ArrayList<>();

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private Long questionId;
        private String question;
        private List<String> options;
        private Integer selectedOption; // 학습자가 고른 번호 (미답이면 null)
        private Integer correctAnswer;
        private boolean correct;
        private String explanation;
    }
}
