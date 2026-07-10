package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CommunityFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    private String originalName;   // 사용자가 업로드한 원본 파일명

    // 이제 로컬 경로가 아니라 Cloudinary secure_url이 통째로 들어감 → length 넉넉하게
    @Column(length = 500)
    private String savedPath;

    private String fileType;       // "IMAGE" | "ATTACH"
    private Long fileSize;         // 바이트 단위

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
}