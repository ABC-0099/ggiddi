package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.Progress;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    @Query("SELECT COUNT(p) > 0 FROM Progress p WHERE p.siteUser = :siteUser AND p.content = :content")
    boolean existsBySiteUserAndContent(@Param("siteUser") SiteUser siteUser, @Param("content") Content content);
    Optional<Progress> findBySiteUserAndContent(SiteUser siteUser, Content content);
    List<Progress> findBySiteUser(SiteUser siteUser);
    Optional<Progress> findTopBySiteUserOrderByUpdatedAtDesc(SiteUser siteUser);

    // ★ 마이페이지 "최근 시청순" 목록용 (완료/진행중 전부 포함, 페이지네이션)
    Page<Progress> findBySiteUserOrderByUpdatedAtDesc(SiteUser siteUser, Pageable pageable);

    // ★ 메인페이지 대시보드 "완료 강의" 카드용
    long countBySiteUserAndCompletedTrue(SiteUser siteUser);
}