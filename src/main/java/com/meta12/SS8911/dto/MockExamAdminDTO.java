package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.MockExam;
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
public class MockExamAdminDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private Integer round;
    private Integer timeLimitMinutes;
    private List<MockExamQuestionAdminDTO> questions;

    public static MockExamAdminDTO from(MockExam exam) {
        return MockExamAdminDTO.builder()
                .id(exam.getId())
                .categoryId(exam.getCategory().getId())
                .categoryName(exam.getCategory().getTitle())
                .title(exam.getTitle())
                .round(exam.getRound())
                .timeLimitMinutes(exam.getTimeLimitMinutes())
                .questions(exam.getQuestions().stream()
                        .map(MockExamQuestionAdminDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }
}