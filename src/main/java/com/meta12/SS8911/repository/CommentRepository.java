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

    // 최상위 댓글은 자기 id, 대댓글은 부모 id를 그룹 키로 묶어서
    // 같은 그룹(부모 + 그 대댓글들)이 이어서 나오도록 정렬한다.
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "LEFT JOIN FETCH c.parent " +
            "WHERE c.community = :community " +
            "ORDER BY CASE WHEN c.parent IS NULL THEN c.id ELSE c.parent.id END ASC, c.createdDate ASC")
    List<Comment> findByCommunityWithAuthor(@Param("community") Community community);

    // 특정 댓글에 달린 대댓글 목록
    List<Comment> findByParent(Comment parent);

    // 내가 쓴 댓글 목록 (마이페이지용) - 게시글 정보도 함께 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.community WHERE c.author = :author ORDER BY c.createdDate DESC")
    List<Comment> findByAuthorOrderByCreatedDateDesc(@Param("author") SiteUser author);

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.community WHERE c.author = :author ORDER BY c.createdDate DESC",
            countQuery = "SELECT count(c) FROM Comment c WHERE c.author = :author")
    Page<Comment> findByAuthorOrderByCreatedDateDesc(@Param("author") SiteUser author, Pageable pageable);
}