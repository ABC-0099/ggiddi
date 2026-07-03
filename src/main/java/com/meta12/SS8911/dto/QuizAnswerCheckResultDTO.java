package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerCheckResultDTO {
    private boolean correct;
    private Integer correctAnswer; // 오답일 때만 채워짐 (정답이면 null)
    private String explanation;
}