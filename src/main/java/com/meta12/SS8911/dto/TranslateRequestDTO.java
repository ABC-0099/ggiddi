package com.meta12.SS8911.dto;

import lombok.Data;

@Data
public class TranslateRequestDTO {
    private String text;
    private String targetLang; // 예: en, zh, ja, vi
}