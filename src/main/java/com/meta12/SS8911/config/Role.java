package com.meta12.SS8911.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("ROLE_ADMIN", "관리자"),
    USER("ROLE_USER", "일반회원"),
    INSTRUCTOR("ROLE_INSTRUCTOR", "강사");

    private final String value;
    private final String description;
}