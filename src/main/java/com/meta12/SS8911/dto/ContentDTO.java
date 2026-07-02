package com.meta12.SS8911.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ContentDTO {

    private Long id;
    private String title;
    private String videoUrl;
    private Integer sequence;
    private Long categoryId;
    private LocalDateTime createdDate;

    // 화면에서 넘어오는 파일
    private MultipartFile videoFile;
    private MultipartFile thumbFile;
    private MultipartFile attachFile;   // 기존 필드 그대로 유지 (레거시, 안 쓰면 그만)

    // 서비스 로직에서 가공된 파일명
    private String fileName;
    private String thumbFileName;
    private String attachFileName;      // 기존 필드 그대로 유지

    // ── 신규 필드 ──
    private Integer stage;
    private String description;
    private String keywords;
    private Integer duration;
    private String status;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime publishAt;
    private boolean free;
    private String videoOriginalName;

    // 다중 첨부파일 (신규 기능)
    private List<MultipartFile> newAttachFiles;
    private List<Long> deleteAttachIds;

    private boolean deleteVideo;
}