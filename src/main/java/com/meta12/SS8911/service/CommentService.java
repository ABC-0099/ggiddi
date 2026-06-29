package com.meta12.SS8911.service;

import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    // 댓글 목록 조회
    public List<Comment> getComments(Community community) {
        return commentRepository.findByCommunityWithAuthor(community);
    }

    // 댓글 등록
    public void create(Community community, String content, SiteUser author) {
        Comment comment = new Comment();
        comment.setCommunity(community);
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setCreatedDate(LocalDateTime.now());
        commentRepository.save(comment);
    }

    // 댓글 단건 조회
    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));
    }

    // 댓글 수정
    public void update(Long id, String content, SiteUser user) {
        Comment comment = getComment(id);
        checkPermission(comment, user);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void delete(Long id, SiteUser user) {
        Comment comment = getComment(id);
        checkPermission(comment, user);
        commentRepository.delete(comment);
    }

    // 작성자 또는 관리자 권한 체크
    private void checkPermission(Comment comment, SiteUser user) {
        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }
}
