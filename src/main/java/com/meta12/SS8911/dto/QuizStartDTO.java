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
public class QuizStartDTO {

    private Long quizId;
    private String title;
    private List<QuizQuestionDTO> questions; // 랜덤으로 뽑힌 N문항, 정답 미포함
}