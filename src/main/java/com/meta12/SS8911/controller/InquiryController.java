package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.InquiryDTO;
import com.meta12.SS8911.entity.Inquiry;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.InquiryService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final SiteUserService siteUserService;

    // 문의 작성 폼
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("inquiryDTO", new InquiryDTO());
        return "inquiry/write";
    }

    // 문의 작성 처리
    @PostMapping("/write")
    public String write(InquiryDTO dto, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        inquiryService.create(dto, user);
        return "redirect:/siteUser/mypage?tab=posts&subtab=myinquiry";
    }

    // 상세보기 (작성자 본인 또는 관리자만)
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Inquiry inquiry = inquiryService.getInquiry(id);
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        inquiryService.checkViewPermission(inquiry, user);

        model.addAttribute("inquiry", inquiry);
        return "inquiry/view";
    }

    // 관리자 답변 등록
    @PostMapping("/{id}/answer")
    public String answer(@PathVariable Long id, @RequestParam String answer, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        inquiryService.answer(id, answer, admin);
        return "redirect:/inquiry/" + id;
    }
}