package com.meta12.SS8911.service;

import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getComments(Community community) {
        return commentRepository.findByCommunityWithAuthor(community);
    }

    public void create(Community community, String content, SiteUser author) {
        Comment comment = new Comment();
        comment.setCommunity(community);
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setCreatedDate(LocalDateTime.now());
        commentRepository.save(comment);
    }

    // 대댓글 작성
    public void createReply(Community community, Long parentId, String content, SiteUser author) {
        Comment parent = getComment(parentId);

        if (!parent.getCommunity().getId().equals(community.getId())) {
            throw new RuntimeException("잘못된 접근입니다.");
        }

        // 대댓글의 대댓글은 최상위 댓글 아래로 묶어서 depth를 1단계로 제한한다.
        Comment topParent = (parent.getParent() != null) ? parent.getParent() : parent;

        Comment reply = new Comment();
        reply.setCommunity(community);
        reply.setContent(content);
        reply.setAuthor(author);
        reply.setCreatedDate(LocalDateTime.now());
        reply.setParent(topParent);
        commentRepository.save(reply);
    }

    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));
    }

    public void update(Long id, String content, SiteUser user) {
        Comment comment = getComment(id);
        checkPermission(comment, user);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    public void delete(Long id, SiteUser user) {
        Comment comment = getComment(id);
        checkPermission(comment, user);

        // 최상위 댓글이면 거기 달린 대댓글도 함께 삭제
        if (comment.getParent() == null) {
            List<Comment> replies = commentRepository.findByParent(comment);
            if (!replies.isEmpty()) {
                commentRepository.deleteAll(replies);
            }
        }

        commentRepository.delete(comment);
    }

    // 내가 쓴 댓글 목록 (마이페이지용)
    public Page<Comment> getCommentsByAuthor(SiteUser author, Pageable pageable) {
        return commentRepository.findByAuthorOrderByCreatedDateDesc(author, pageable);
    }

    private void checkPermission(Comment comment, SiteUser user) {
        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }
}