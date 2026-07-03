package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.MockExamQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockExamQuestionDTO {

    private Long id;
    private String question;
    private String questionType;
    private List<String> options;

    public static MockExamQuestionDTO from(MockExamQuestion q) {
        return MockExamQuestionDTO.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .questionType(q.getQuestionType())
                .options(List.of(q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()))
                .build();
    }
}