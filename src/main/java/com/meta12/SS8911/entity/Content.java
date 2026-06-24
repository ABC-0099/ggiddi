package com.meta12.SS8911.entity;


import jakarta.persistence.*;
;
import lombok.Getter;
import lombok.Setter;

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

    private String fileOrigin;

    // --- DB에 저장될 파일명들을 따로 관리 (덮어쓰기 방지) ---
    private String fileName;       // 영상 저장 파일명
    private String thumbFileName;  // 썸네일 저장 파일명 (추가됨)
    private String attachFileName; // 첨부파일 저장 파일명 (추가됨)

    @Transient
    private Integer progressPercent;

    public int getProgressPercent() {
        return (progressPercent == null) ? 0 : progressPercent;
    }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }




}
