package com.meta12.SS8911.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Content {

    //세부영상
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String title;
    private String videoUrl;
    private Integer sequence;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    private LocalDateTime createdDate;


    private String fileOrigin;      // 영상 원본 파일명
    private String attachFileOrigin; // 첨부파일 원본 파일명 (fileOrigin과 분리)

    // --- DB에 저장될 파일명(이제는 Cloudinary URL)들을 따로 관리 (덮어쓰기 방지) ---
    // Cloudinary secure_url이 로컬 파일명보다 훨씬 길어서 length를 넉넉하게 늘려둠
    @Column(length = 500)
    private String fileName;       // 영상 URL

    @Column(length = 500)
    private String thumbFileName;  // 썸네일 URL

    @Column(length = 500)
    private String attachFileName; // 첨부파일 URL

    private Integer stage;

    private String description;
    private String keywords;
    private String status;         // DRAFT / PUBLISHED / SCHEDULED
    private LocalDateTime publishAt;
    private boolean free;

    @Transient
    private Integer progressPercent;

    public int getProgressPercent() {
        return (progressPercent == null) ? 0 : progressPercent;
    }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    @OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

}