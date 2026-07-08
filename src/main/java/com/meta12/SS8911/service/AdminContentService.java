package com.meta12.SS8911.service;

import com.meta12.SS8911.entity.AdminContent;
import com.meta12.SS8911.repository.AdminContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final AdminContentRepository adminContentRepository;

    // 1. DB에 저장된 실제 콘텐츠 전체 조회
    public List<AdminContent> getAllAdminContents() {
        return adminContentRepository.findAll();
    }

    // 2. 화면에서 입력한 콘텐츠 데이터를 DB에 진짜로 저장(Insert)
    @Transactional
    public AdminContent saveContent(AdminContent adminContent) {
        return adminContentRepository.save(adminContent);
    }
}