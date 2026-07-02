package com.meta12.SS8911.dto;

import com.meta12.SS8911.entity.SiteUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NoticeDTO {

    private Long id;

    private String title;

    private String content;


    private SiteUser author;

    private LocalDateTime createdAt;

    // 이 필드를 추가해야 sujung.html의 <select> 박스가 정상 작동합니다.
    private String category;

    public boolean isRecentPost() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(2));
    }
}