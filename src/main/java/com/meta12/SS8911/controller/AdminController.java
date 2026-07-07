package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final QnaService qnaService;
    private final SiteUserService siteUserService;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        // 게시판 관리 > 질문하기 탭에 쓸 데이터
        Page<Qna> qnas = qnaService.getAllQnas(PageRequest.of(0, 20));
        model.addAttribute("qnas", qnas);
        model.addAttribute("pendingQnaCount", qnaService.countPending());

        return "admin/main";
    }

    // 게시판 관리 > 질문하기 탭 > 신고/미답변 게시글 삭제
    @PostMapping("/admin/board/qna/{id}/delete")
    public String qnaDelete(@PathVariable Long id, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        qnaService.adminDelete(id, admin);
        return "redirect:/admin?panel=board&tab=qna";
    }

}