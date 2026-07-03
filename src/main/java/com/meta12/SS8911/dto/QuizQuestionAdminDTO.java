package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionAdminDTO {

    private Long id;
    private String question;
    private String questionType;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private Integer answer;
    private String explanation;

    public static QuizQuestionAdminDTO from(QuizQuestion q) {
        return QuizQuestionAdminDTO.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .questionType(q.getQuestionType())
                .option1(q.getOption1())
                .option2(q.getOption2())
                .option3(q.getOption3())
                .option4(q.getOption4())
                .answer(q.getAnswer())
                .explanation(q.getExplanation())
                .build();
    }
}