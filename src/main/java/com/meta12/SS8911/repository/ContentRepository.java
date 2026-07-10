package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByCategoryIdOrderBySequenceAsc(Long courseId);

    Optional<Content> findTopByCategoryIdAndIdLessThanOrderByIdDesc(Long categoryId, Long id);
    // 기존 ContentRepository.java 인터페이스 안에 이 메서드 한 줄만 추가하면 됩니다.
// (findTopByCategoryIdAndIdLessThanOrderByIdDesc 는 더 이상 안 씁니다 - sequence 기준으로 교체됨)

    Optional<Content> findTopByCategoryIdAndSequenceLessThanOrderBySequenceDesc(Long categoryId, Integer sequence);

    // ★ 메인페이지 대시보드 "전체 진도율" 분모용 (공개된 강의 전체 개수)
    long countByStatus(String status);

}