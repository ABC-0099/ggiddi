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
        public String list(@RequestParam(required = false) Category category, Model model) {
            List<Community> posts = communityService.getCommunityPosts(category);
            model.addAttribute("posts", posts);
            model.addAttribute("category", category);
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
            return "community/detail";
        }
    }
