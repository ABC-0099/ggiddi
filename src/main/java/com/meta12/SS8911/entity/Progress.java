package com.meta12.SS8911.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Progress {

    //영상 수강률바
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    private boolean completed;
    private LocalDateTime completedAt;

    @ManyToOne
    private SiteUser siteUser;

    private Double lastWatchedTime; // 초 단위로 기록
    private Double percentage;      // 0 ~ 100 사이 값

}
