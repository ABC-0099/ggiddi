package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {

    private Long id;
    private String title;
    private Long contentId;
    private List<QuizQuestionDTO> questions;

    public static QuizDTO from(Quiz quiz) {
        return QuizDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .contentId(quiz.getContent().getId())
                .questions(quiz.getQuestions().stream()
                        .map(QuizQuestionDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }
}