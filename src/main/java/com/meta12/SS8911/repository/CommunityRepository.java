package com.meta12.SS8911.repository;

import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    // 카테고리별로 최신순 조회
    List<Community> findByCategoryOrderByCreatedDateDesc(Category category);

    // 전체 최신순 조회
    List<Community> findAllByOrderByCreatedDateDesc();

    List<Community> findByAuthorOrderByCreatedDateDesc(SiteUser author);
}
