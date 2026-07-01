package com.meta12.SS8911.dto;

import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.entity.CommunityFile;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
public class CommunityDTO {
    private String title;
    private String content;
    private Category category;

    List<MultipartFile> imageFiles;
    List<MultipartFile> attachFiles;

    private List<Long> deleteFileIds; // 수정 시 삭제할 기존 첨부파일 ID 목록

    // 수정 페이지에서 기존 파일 목록 보여줄 때 채워지는 필드 (폼 전송 시에는 사용 안 함)
    private List<CommunityFile> existingImages;
    private List<CommunityFile> existingFiles;
}