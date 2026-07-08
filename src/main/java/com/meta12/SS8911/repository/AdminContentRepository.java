package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.AdminContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminContentRepository extends JpaRepository<AdminContent, Long> {
}