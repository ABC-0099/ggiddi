package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    Page<Qna> findByAuthorOrderByCreatedDateDesc(SiteUser author, Pageable pageable);

    Page<Qna> findAllByOrderByCreatedDateDesc(Pageable pageable);
}