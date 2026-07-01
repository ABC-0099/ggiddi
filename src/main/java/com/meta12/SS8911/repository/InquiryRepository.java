package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Inquiry;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByAuthorOrderByCreatedDateDesc(SiteUser author, Pageable pageable);

    // 관리자용: 전체 문의 목록 (나중에 관리자 페이지 만들 때 사용)
    Page<Inquiry> findAllByOrderByCreatedDateDesc(Pageable pageable);
}