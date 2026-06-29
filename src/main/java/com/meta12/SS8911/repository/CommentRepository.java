package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.community = :community ORDER BY c.createdDate ASC")
    List<Comment> findByCommunityWithAuthor(@Param("community") Community community);
}
