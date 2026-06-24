package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MypageRepository extends JpaRepository<Community, Long> {
    List<Community> findByAuthorOrderByCreatedDateDesc(SiteUser author);
}