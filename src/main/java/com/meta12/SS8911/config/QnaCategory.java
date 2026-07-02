package com.meta12.SS8911.config;

public enum QnaCategory {
    ACCOUNT("계정/로그인"),
    PAYMENT("결제/환불"),
    CONTENT("학습 콘텐츠"),
    COMMUNITY("커뮤니티/신고"),
    BUG("버그/오류 신고"),
    PARTNERSHIP("제휴/제안"),
    ETC("기타");

    private final String description;
    QnaCategory(String description) { this.description = description; }
    public String getDescription() { return description; }
}
