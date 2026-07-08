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

    // ── 💡 콘텐츠 수정 로직 추가 ──
    @org.springframework.transaction.annotation.Transactional
    public void updateContent(Long id, String title, String step, Integer lectureCount, String status) {
        // 1. DB에서 수정할 콘텐츠를 찾습니다. 없으면 에러를 발생시킵니다.
        AdminContent content = adminContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠가 존재하지 않습니다. id=" + id));

        // 2. 화면에서 넘어온 새 값으로 데이터를 변경합니다.
        content.setTitle(title);
        content.setStep(step);
        content.setLectureCount(lectureCount);
        content.setStatus(status);

        // @Transactional이 붙어있으므로 메서드가 끝날 때 DB에 자동으로 저장(Update)됩니다.
    }

    // ── 💡 콘텐츠 삭제 로직 추가 ──
    @org.springframework.transaction.annotation.Transactional
    public void deleteContent(Long id) {
        // 1. DB에서 삭제할 콘텐츠를 찾습니다.
        AdminContent content = adminContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠가 존재하지 않습니다. id=" + id));

        // 2. 찾아온 콘텐츠를 DB에서 완전히 지웁니다.
        adminContentRepository.delete(content);
    }
}