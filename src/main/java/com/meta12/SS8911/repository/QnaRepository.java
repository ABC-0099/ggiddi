package com.meta12.SS8911.repository;

import com.meta12.SS8911.config.InquiryStatus;
import com.meta12.SS8911.config.QnaCategory;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 회원 마이페이지: 내가 쓴 문의 목록
    Page<Qna> findByAuthorOrderByCreatedDateDesc(SiteUser author, Pageable pageable);

    // 관리자 마이페이지: 전체 문의 목록
    Page<Qna> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // 관리자 마이페이지: 상태별(답변대기/답변완료) 목록
    Page<Qna> findByStatusOrderByCreatedDateDesc(InquiryStatus status, Pageable pageable);

    // 게시판 관리 탭 배지: 상태별 개수
    long countByStatus(InquiryStatus status);

    // 카테고리 + 검색
    @Query("""
        SELECT q
        FROM Qna q
        WHERE q.category = :category
          AND (:kw = '' OR q.title LIKE %:kw% OR q.content LIKE %:kw%)
        ORDER BY q.createdDate DESC
        """)
    Page<Qna> findByCategoryAndKeyword(
            @Param("category") QnaCategory category,
            @Param("kw") String kw,
            Pageable pageable);

    // 전체 카테고리 + 검색
    @Query("""
        SELECT q
        FROM Qna q
        WHERE (:kw = '' OR q.title LIKE %:kw% OR q.content LIKE %:kw%)
        ORDER BY q.createdDate DESC
        """)
    Page<Qna> findByKeyword(
            @Param("kw") String kw,
            Pageable pageable);
}