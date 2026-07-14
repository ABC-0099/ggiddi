package com.meta12.SS8911.repository;

import com.meta12.SS8911.config.SourceType;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.entity.WrongAnswerNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrongAnswerNoteRepository extends JpaRepository<WrongAnswerNote, Long> {

    // 마이페이지 오답노트 탭: 최근 틀린 순, 페이지네이션
    Page<WrongAnswerNote> findBySiteUserOrderByCreatedDateDesc(SiteUser siteUser, Pageable pageable);

    // 출처(연습퀴즈/모의고사)별 필터링
    Page<WrongAnswerNote> findBySiteUserAndSourceTypeOrderByCreatedDateDesc(
            SiteUser siteUser, SourceType sourceType, Pageable pageable);

    // 복습 여부만 필터링 (전체 소스타입 대상)
    Page<WrongAnswerNote> findBySiteUserAndReviewedOrderByCreatedDateDesc(
            SiteUser siteUser, boolean reviewed, Pageable pageable);

    // 출처 + 복습 여부 둘 다 필터링
    Page<WrongAnswerNote> findBySiteUserAndSourceTypeAndReviewedOrderByCreatedDateDesc(
            SiteUser siteUser, SourceType sourceType, boolean reviewed, Pageable pageable);

    // 관리자 통계용: 전체 오답 목록 (카테고리/단계별 집계에 사용)
    List<WrongAnswerNote> findBySourceType(SourceType sourceType);

    // 아직 복습 안 한 오답 개수 (마이페이지 배지 등에 활용 가능)
    long countBySiteUserAndReviewedFalse(SiteUser siteUser);
}