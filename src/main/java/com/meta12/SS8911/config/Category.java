package com.meta12.SS8911.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    ALL("전체"), // 전체 보기용
    CULTURAL("문화 교류"),
    REVIEW("학습 후기"),
    FREE("자유게시판");

    private final String description;
}
