package com.meta12.SS8911.entity;

import com.meta12.SS8911.config.InquiryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;

    private LocalDateTime createdDate;
    private LocalDateTime answeredDate;

    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) this.createdDate = LocalDateTime.now();
        if (this.status == null) this.status = InquiryStatus.PENDING;
    }
}