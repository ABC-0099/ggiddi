package com.meta12.SS8911.controller;


import com.meta12.SS8911.dto.SiteUserDTO;


import com.meta12.SS8911.dto.SiteUserEditDTO;
import com.meta12.SS8911.entity.Comment;
import com.meta12.SS8911.entity.Community;

import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.CommentService;
import com.meta12.SS8911.service.CommunityService;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SiteUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SiteUserController {
    private final SiteUserService siteUserService;
    private final CommunityService communityService;
    private final CommentService commentService;
    private final QnaService qnaService;

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
                         @RequestParam(defaultValue = "0") int commentPage,
                         @RequestParam(defaultValue = "0") int inquiryPage) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        Pageable postPageable = PageRequest.of(postPage, 5);
        Page<Community> myPosts = communityService.getPostsByAuthor(user, postPageable);

        Pageable commentPageable = PageRequest.of(commentPage, 5);
        Page<Comment> myComments = commentService.getCommentsByAuthor(user, commentPageable);

        Pageable qnaPageable = PageRequest.of(inquiryPage, 5);
        Page<Qna> myInquiries = qnaService.getMyQnas(user, qnaPageable);

        model.addAttribute("siteUser", user);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("myComments", myComments);
        model.addAttribute("myInquiries", myInquiries);
        return "siteUser/mypage";
    }

    // 정보 수정 처리
    @PostMapping("/siteUser/editProc")
    public String editProc(@Valid @ModelAttribute("siteUserEditDTO") SiteUserEditDTO dto,
                           BindingResult bindingResult,
                           Principal principal,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "siteUser/edit";
        }

        try {
            siteUserService.updateInfo(principal.getName(), dto);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "siteUser/edit";
        }

        return "redirect:/siteUser/mypage";
    }

    // 회원 탈퇴 처리
    @PostMapping("/siteUser/withdraw")
    public String withdraw(@RequestParam String currentPassword,
                           Principal principal,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        try {
            siteUserService.withdraw(principal.getName(), currentPassword);
        } catch (IllegalStateException e) {
            return "redirect:/siteUser/mypage?withdrawError=true";
        }

        // 탈퇴 처리 후 강제 로그아웃
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/?withdrawn=true";
    }

    @GetMapping("/siteUser/edit")
    public String editForm(Model model, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        // 1. DTO 생성 및 값 세팅 (기존 코드)
        SiteUserEditDTO dto = new SiteUserEditDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setBirth(user.getBirth());
        dto.setNationality(user.getNationality());

        // 2. 템플릿에서 ${siteUser.username}을 사용하므로,
        // 아래 코드를 추가해야 에러가 사라집니다.
        model.addAttribute("siteUser", user);
        model.addAttribute("siteUserEditDTO", dto);

        return "siteUser/edit";
    }
}