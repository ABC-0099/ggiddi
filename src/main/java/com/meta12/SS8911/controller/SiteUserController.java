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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

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

    @GetMapping("/siteUser/edit")
    public String editForm(Model model, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        SiteUserEditDTO dto = new SiteUserEditDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setBirth(user.getBirth());
        dto.setNationality(user.getNationality());

        System.out.println("siteUser : " + user);

        model.addAttribute("siteUser", user);
        model.addAttribute("siteUserEditDTO", dto);

        return "siteUser/edit";
    }

    // 수정 완료: 중복된 editProc 메서드를 하나로 통합
    @PostMapping("/siteUser/editProc")
    public String editProc(
            @Valid @ModelAttribute("siteUserEditDTO") SiteUserEditDTO dto,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes rttr) {

        if (bindingResult.hasErrors()) {

            System.out.println("===== Validation Errors =====");

            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println(
                        "field = " + error.getField()
                                + ", rejectedValue = " + error.getRejectedValue()
                                + ", message = " + error.getDefaultMessage()
                );
            });

            SiteUser user = siteUserService.getUserByUsername(principal.getName());
            model.addAttribute("siteUser", user);

            return "siteUser/edit";
        }

        try {
            siteUserService.editProc(principal.getName(), dto);
            rttr.addFlashAttribute("successMsg", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/siteUser/edit";
        }

        return "redirect:/siteUser/mypage";
    }

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

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/?withdrawn=true";
    }
}