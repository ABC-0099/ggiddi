package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.community = :community ORDER BY c.createdDate ASC")
    List<Comment> findByCommunityWithAuthor(@Param("community") Community community);

    // 내가 쓴 댓글 목록 (마이페이지용) - 게시글 정보도 함께 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.community WHERE c.author = :author ORDER BY c.createdDate DESC")
    List<Comment> findByAuthorOrderByCreatedDateDesc(@Param("author") SiteUser author);

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.community WHERE c.author = :author ORDER BY c.createdDate DESC",
            countQuery = "SELECT count(c) FROM Comment c WHERE c.author = :author")
    Page<Comment> findByAuthorOrderByCreatedDateDesc(@Param("author") SiteUser author, Pageable pageable);
}
