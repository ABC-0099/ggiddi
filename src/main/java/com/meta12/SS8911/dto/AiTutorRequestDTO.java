package com.meta12.SS8911.dto;

import lombok.Data;

@Data
public class AiTutorRequestDTO {
    private String question;
    private String lectureTitle;
    private String lectureContent;
    private Integer videoTimestamp; // 초 단위, null 가능
}