package com.meta12.SS8911.repository;

import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    // 1. [카테고리 + 검색] 검색어(제목/내용)가 포함된 게시글 조회
    @Query("SELECT c FROM Community c JOIN FETCH c.author " +
            "WHERE c.category = :category " +
            "AND (c.title LIKE %:kw% OR c.content LIKE %:kw%) " +
            "ORDER BY c.createdDate DESC")
    List<Community> findByCategoryAndKeyword(@Param("category") Category category, @Param("kw") String kw);

    // 2. [전체 + 검색] 카테고리 상관없이 검색어(제목/내용)가 포함된 게시글 조회
    @Query("SELECT c FROM Community c JOIN FETCH c.author " +
            "WHERE (c.title LIKE %:kw% OR c.content LIKE %:kw%) " +
            "ORDER BY c.createdDate DESC")
    List<Community> findAllByKeyword(@Param("kw") String kw);

    // 전체 최신순 조회 (author fetch join)
    @Query("SELECT c FROM Community c JOIN FETCH c.author ORDER BY c.createdDate DESC")
    List<Community> findAllWithAuthor();

    // 전체 등록순 조회 (author fetch join)
    @Query("SELECT c FROM Community c JOIN FETCH c.author ORDER BY c.createdDate ASC")
    List<Community> findAllWithAuthorOldest();

    // 카테고리별 최신순 조회 (author fetch join)
    @Query("SELECT c FROM Community c JOIN FETCH c.author WHERE c.category = :category ORDER BY c.createdDate DESC")
    List<Community> findByCategoryWithAuthor(@Param("category") Category category);

    // 카테고리별 등록순 조회 (author fetch join)
    @Query("SELECT c FROM Community c JOIN FETCH c.author WHERE c.category = :category ORDER BY c.createdDate ASC")
    List<Community> findByCategoryWithAuthorOldest(@Param("category") Category category);

    // 카테고리별로 최신순 조회
    List<Community> findByCategoryOrderByCreatedDateDesc(Category category);

    // 전체 최신순 조회
    List<Community> findAllByOrderByCreatedDateDesc();

    List<Community> findByAuthorOrderByCreatedDateDesc(SiteUser author);

    Page<Community> findByAuthorOrderByCreatedDateDesc(SiteUser author, Pageable pageable);

    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author " +
            "WHERE c.category = :category " +
            "AND (c.title LIKE %:kw% OR c.content LIKE %:kw%) " +
            "ORDER BY c.createdDate DESC",
            countQuery = "SELECT count(c) FROM Community c " +
                    "WHERE c.category = :category " +
                    "AND (c.title LIKE %:kw% OR c.content LIKE %:kw%)")
    Page<Community> findByCategoryAndKeyword(@Param("category") Category category, @Param("kw") String kw, Pageable pageable);

    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author " +
            "WHERE (c.title LIKE %:kw% OR c.content LIKE %:kw%) " +
            "ORDER BY c.createdDate DESC",
            countQuery = "SELECT count(c) FROM Community c " +
                    "WHERE (c.title LIKE %:kw% OR c.content LIKE %:kw%)")
    Page<Community> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author ORDER BY c.createdDate DESC",
            countQuery = "SELECT count(c) FROM Community c")
    Page<Community> findAllWithAuthor(Pageable pageable);

    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author ORDER BY c.createdDate ASC",
            countQuery = "SELECT count(c) FROM Community c")
    Page<Community> findAllWithAuthorOldest(Pageable pageable);

    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author WHERE c.category = :category ORDER BY c.createdDate DESC",
            countQuery = "SELECT count(c) FROM Community c WHERE c.category = :category")
    Page<Community> findByCategoryWithAuthor(@Param("category") Category category, Pageable pageable);

    @Query(value = "SELECT c FROM Community c JOIN FETCH c.author WHERE c.category = :category ORDER BY c.createdDate ASC",
            countQuery = "SELECT count(c) FROM Community c WHERE c.category = :category")
    Page<Community> findByCategoryWithAuthorOldest(@Param("category") Category category, Pageable pageable);
}