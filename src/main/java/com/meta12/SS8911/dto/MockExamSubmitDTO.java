package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MockExamSubmitDTO {

    private Long examId;
    private Integer elapsedSeconds; // 실제 소요 시간(선택)
    private List<AnswerEntry> answers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerEntry {
        private Long questionId;
        private Integer selectedOption; // null이면 미답
    }
}