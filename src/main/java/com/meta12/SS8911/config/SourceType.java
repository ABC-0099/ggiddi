package com.meta12.SS8911.config;

public enum SourceType {
    PRACTICE_QUIZ("연습퀴즈"),
    MOCK_EXAM("실전모의고사");

    private final String description;

    SourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}