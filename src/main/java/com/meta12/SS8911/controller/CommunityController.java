package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.CommunityDTO;
import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.CommentService;
import com.meta12.SS8911.service.CommunityService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final SiteUserService siteUserService;
    private final CommentService commentService;

    // 목록
    @GetMapping
    public String list(@RequestParam(required = false) Category category,
                       @RequestParam(required = false, defaultValue = "newest") String sort,
                       @RequestParam(required = false, defaultValue = "") String kw,
                       @RequestParam(required = false, defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Community> posts = communityService.getCommunityPosts(category, sort, kw, pageable);
        model.addAttribute("postList", posts);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("kw", kw);
        return "community/list";
    }

    // 글쓰기 폼
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("communityDTO", new CommunityDTO());
        return "community/write";
    }

    // 글쓰기 처리
    @PostMapping("/write")
    public String write(CommunityDTO dto, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        communityService.create(dto, user);
        return "redirect:/community";
    }

    // 상세보기 - 댓글 목록 추가
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Community post = communityService.getPost(id);
        List<Comment> comments = commentService.getComments(post);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        return "community/view";
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Community post = communityService.getPost(id);
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        if (!isAuthor) {
            return "redirect:/community/" + id;
        }

        CommunityDTO dto = new CommunityDTO();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());

        dto.setExistingImages(post.getFiles().stream()
                .filter(f -> "IMAGE".equals(f.getFileType()))
                .collect(java.util.stream.Collectors.toList()));
        dto.setExistingFiles(post.getFiles().stream()
                .filter(f -> "ATTACH".equals(f.getFileType()))
                .collect(java.util.stream.Collectors.toList()));

        model.addAttribute("communityDTO", dto);
        model.addAttribute("postId", id);
        return "community/edit";
    }

    // 수정 처리
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, CommunityDTO dto, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        communityService.update(id, dto, user);
        return "redirect:/community/" + id;
    }

    // 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        communityService.delete(id, user);
        return "redirect:/community";
    }
}
