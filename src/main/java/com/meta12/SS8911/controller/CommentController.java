package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.CommentService;
import com.meta12.SS8911.service.CommunityService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/community/{postId}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommunityService communityService;
    private final SiteUserService siteUserService;

    // 댓글 등록
    @PostMapping
    public String create(@PathVariable Long postId,
                         @RequestParam String content,
                         Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        commentService.create(communityService.getPost(postId), content, user);
        return "redirect:/community/" + postId;
    }

    // 댓글 수정
    @PostMapping("/{commentId}/edit")
    public String edit(@PathVariable Long postId,
                       @PathVariable Long commentId,
                       @RequestParam String content,
                       Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        commentService.update(commentId, content, user);
        return "redirect:/community/" + postId;
    }

    // 댓글 삭제
    @PostMapping("/{commentId}/delete")
    public String delete(@PathVariable Long postId,
                         @PathVariable Long commentId,
                         Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        commentService.delete(commentId, user);
        return "redirect:/community/" + postId;
    }
}
