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
public class MockExamStartDTO {

    private Long examId;
    private String title;
    private int timeLimitMinutes;
    private List<MockExamQuestionDTO> questions;
}