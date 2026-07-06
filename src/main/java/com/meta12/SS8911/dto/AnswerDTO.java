package com.meta12.SS8911.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerDTO {

    // 답변 대상 질문 id (edit/delete에서 리다이렉트 경로 만들 때 필요)
    private Long qnaId;

    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(max = 2000, message = "답변은 2000자 이내로 작성해주세요.")
    private String content;
}