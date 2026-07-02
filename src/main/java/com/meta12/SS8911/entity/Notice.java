package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Getter
@Setter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 추가: DTO 및 수정 페이지와 일치하도록 필드 추가
    private String category;

    private String title;


    @Column(columnDefinition = "TEXT")
    private String content;

    // 수정: String이 아닌 SiteUser 객체로 변경하여 에러 해결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SiteUser author;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isRecentPost() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(2));
    }
}
