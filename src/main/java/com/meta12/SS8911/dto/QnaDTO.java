package com.meta12.SS8911.dto;

import com.meta12.SS8911.config.QnaCategory;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QnaDTO {
    private String title;
    private String content;
    private QnaCategory category; // getter, setter도 포함하세요
}