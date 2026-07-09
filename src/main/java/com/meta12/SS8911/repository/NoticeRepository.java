package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByCategory(String category, Pageable pageable);

    // 🌟 [추가]: 제목 검색용
    Page<Notice> findByTitleContaining(String keyword, Pageable pageable);

    // 🌟 [추가]: 카테고리 필터 + 제목 검색 동시에 쓸 때
    Page<Notice> findByCategoryAndTitleContaining(String category, String keyword, Pageable pageable);
}