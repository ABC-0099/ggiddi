package com.meta12.SS8911.config;


import lombok.Getter;

@Getter
public enum OrderPayStatus {
    //결제용
    SUCCESS("SUCCESS", "결제 완료"),
    CANCEL("CANCEL", "결제 취소"),
    FAILED("FAILED", "결제 실패");

    private final String value;
    private final String description;

    OrderPayStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
