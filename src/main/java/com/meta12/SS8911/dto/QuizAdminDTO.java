package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAdminDTO {

    private Long id;
    private Long contentId;
    private String contentTitle;
    private String categoryName; // ★ 소속 테마명 (예: "케이팝") - 관리자 목록에서 테마 구분용
    private String title;
    private Integer questionCount; // ★ 문제은행에서 매 시도마다 뽑을 문항 수
    private List<QuizQuestionAdminDTO> questions;

    public static QuizAdminDTO from(Quiz quiz) {
        return QuizAdminDTO.builder()
                .id(quiz.getId())
                .contentId(quiz.getContent().getId())
                .contentTitle(quiz.getContent().getTitle())
                .categoryName(quiz.getContent().getCategory().getTitle())
                .title(quiz.getTitle())
                .questionCount(quiz.getQuestionCount())
                .questions(quiz.getQuestions().stream()
                        .map(QuizQuestionAdminDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }
}