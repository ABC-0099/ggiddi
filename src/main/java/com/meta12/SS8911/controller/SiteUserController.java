package com.meta12.SS8911.controller;

import com.meta12.SS8911.Dto.SiteUserDTO;
import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.CommentService;
import com.meta12.SS8911.service.CommunityService;
import com.meta12.SS8911.service.SiteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SiteUserController {
    private final SiteUserService siteUserService;
    private final CommunityService communityService;
    private final CommentService commentService;

    @GetMapping("/siteUser/chuga")
    public String chugaForm(SiteUserDTO siteUserDTO) {
        return "siteUser/chuga";
    }

    @PostMapping("/siteUser/chugaProc")
    public String chugaProc(@Valid SiteUserDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "siteUser/chuga";
        if (!dto.getPassword().equals(dto.getPasswordChk())) {
            bindingResult.rejectValue("passwordChk", "error", "비밀번호가 일치하지 않습니다.");
            return "siteUser/chuga";
        }
        siteUserService.chugaProc(dto);
        return "redirect:/siteUser/login";
    }

    @GetMapping("/siteUser/profile/{username}")
    public String profile(@PathVariable("username") String username, Model model) {
        SiteUser user = siteUserService.getUserByUsername(username);
        model.addAttribute("siteUser", user);
        return "siteUser/view";
    }

    @GetMapping("/siteUser/login")
    public String login() {
        return "siteUser/login";
    }

    @GetMapping("/siteUser/mypage")
    public String mypage(Model model, Principal principal,
                         @RequestParam(defaultValue = "0") int postPage,
                         @RequestParam(defaultValue = "0") int commentPage) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        // ★ 게시글: 페이지당 5개
        Pageable postPageable = PageRequest.of(postPage, 5);
        Page<Community> myPosts = communityService.getPostsByAuthor(user, postPageable);

        // ★ 댓글: 페이지당 5개
        Pageable commentPageable = PageRequest.of(commentPage, 5);
        Page<Comment> myComments = commentService.getCommentsByAuthor(user, commentPageable);

        model.addAttribute("siteUser", user);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("myComments", myComments);
        return "siteUser/mypage";
    }
}
