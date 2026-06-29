package com.meta12.SS8911.controller;

import com.meta12.SS8911.Dto.CommunityDTO;
import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.CommunityService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
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

    // 목록
    @GetMapping
    public String list(@RequestParam(required = false) Category category,
                       @RequestParam(required = false, defaultValue = "newest") String sort,
                       Model model) {
        List<Community> posts = communityService.getCommunityPosts(category, sort);
        model.addAttribute("postList", posts);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
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

    // 상세보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Community post = communityService.getPost(id);
        model.addAttribute("post", post);
        return "community/view";
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Community post = communityService.getPost(id);
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        // 권한 체크 (작성자 또는 관리자)
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAuthor && !isAdmin) {
            return "redirect:/community/" + id;
        }

        CommunityDTO dto = new CommunityDTO();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());

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