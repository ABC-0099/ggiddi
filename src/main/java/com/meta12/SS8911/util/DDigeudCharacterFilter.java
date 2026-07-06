package com.meta12.SS8911.util;

public class DDigeudCharacterFilter {

    // 한글, 영문, 숫자, 기본 문장부호만 허용 (이모지 제거)
    private static final String ALLOWED_PATTERN = "[^가-힣a-zA-Z0-9\\s.,!?~()%\\-'\"]";

    public static String filter(String text) {
        if (text == null) return "";
        return text.replaceAll(ALLOWED_PATTERN, "").trim();
    }
}