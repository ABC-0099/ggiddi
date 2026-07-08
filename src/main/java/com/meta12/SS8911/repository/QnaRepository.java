package com.meta12.SS8911.repository;

import com.meta12.SS8911.config.InquiryStatus;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 💡 추가됨
import org.springframework.data.repository.query.Param; // 💡 추가됨

public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 회원 마이페이지: 내가 쓴 문의 목록
    Page<Qna> findByAuthorOrderByCreatedDateDesc(SiteUser author, Pageable pageable);

    // 관리자 마이페이지: 전체 문의 목록
    Page<Qna> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // 관리자 마이페이지: 상태별(답변대기/답변완료) 목록
    Page<Qna> findByStatusOrderByCreatedDateDesc(InquiryStatus status, Pageable pageable);

    // 게시판 관리 탭 배지: 상태별 개수 (예: 미답변 3)
    long countByStatus(InquiryStatus status);

    // ── 💡 여기에 이 메서드 '딱 하나만' 새로 추가해 주세요 (기존 코드 유지) ──
    // ── 💡 파라미터 타입을 String에서 QnaCategory로 수정합니다 (기존 원본 코드 유지) ──
    @Query("SELECT q FROM Qna q WHERE q.category = :category " +
            "AND (q.title LIKE %:kw% OR q.content LIKE %:kw%) ORDER BY q.createdDate DESC")
    Page<Qna> findByCategoryAndKeyword(@Param("category") com.meta12.SS8911.config.QnaCategory category,
                                       @Param("kw") String kw,
                                       Pageable pageable);
}