package com.meta12.SS8911.dto;

import com.meta12.SS8911.config.QnaCategory;
import com.meta12.SS8911.entity.QnaFile;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
public class QnaDTO {
    private String title;
    private String content;
    private QnaCategory category; // getter, setter도 포함하세요

    private List<MultipartFile> images;

    // 수정 시 삭제할 기존 이미지 ID 목록
    private List<Long> deleteFileIds;

    // 수정 페이지에서 기존 이미지 보여줄 때만 사용 (폼 전송 시엔 무시됨)
    private List<QnaFile> existingImages;
}