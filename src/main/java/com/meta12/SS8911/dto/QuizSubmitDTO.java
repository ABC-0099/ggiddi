package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitDTO {

    private Long quizId;
    private List<AnswerEntry> answers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerEntry {
        private Long questionId;
        private Integer selectedOption; // 학습자가 고른 번호 1~4
    }
}