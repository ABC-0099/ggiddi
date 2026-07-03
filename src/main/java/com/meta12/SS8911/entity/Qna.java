package com.meta12.SS8911.entity;

import com.meta12.SS8911.config.InquiryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.meta12.SS8911.config.QnaCategory;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QnaCategory category;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) this.createdDate = LocalDateTime.now();
        if (this.status == null) this.status = InquiryStatus.PENDING;
        if (this.category == null) this.category = QnaCategory.ETC;
    }

    @OneToMany(mappedBy = "qna", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaFile> files = new ArrayList<>();

    // 질문 하나에 답변 여러 개 가능
    @OneToMany(mappedBy = "qna", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();
}