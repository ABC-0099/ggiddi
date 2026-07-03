package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;

    // 어떤 질문(Qna)에 대한 답변인지 (질문 하나에 답변 여러 개 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_id")
    private Qna qna;

    // 답변을 작성한 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser admin;

    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) this.createdDate = LocalDateTime.now();
    }
}