package com.meta12.SS8911.repository;

import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByUsername(String username);

    boolean existsByUsername(String username);

    // 관리자 대시보드: 오늘 가입한 회원 수
    long countByJoinDateBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByPhone(String phone);

    // ★ 회원 관리 목록용 - 관리자(ADMIN) 계정은 빼고 조회
    Page<SiteUser> findByRoleNot(Role role, Pageable pageable);
}