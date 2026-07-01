package com.meta12.SS8911.config;

public enum InquiryStatus {
    PENDING("답변대기"),
    ANSWERED("답변완료");

    private final String description;

    InquiryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}