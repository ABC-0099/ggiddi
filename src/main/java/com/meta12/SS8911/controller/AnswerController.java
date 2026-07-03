package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.AnswerService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final SiteUserService siteUserService;

    // 답변 작성 (관리자 마이페이지에서 특정 문의에 답변)
    @PostMapping("/qna/{qnaId}/answer")
    public String create(@PathVariable Long qnaId, @RequestParam String content, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        answerService.create(qnaId, content, admin);
        return "redirect:/qna/" + qnaId;
    }

    // 답변 수정
    @PostMapping("/answer/{answerId}/edit")
    public String edit(@PathVariable Long answerId, @RequestParam String content,
                       @RequestParam Long qnaId, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        answerService.update(answerId, content, admin);
        return "redirect:/qna/" + qnaId;
    }

    // 답변 삭제
    @PostMapping("/answer/{answerId}/delete")
    public String delete(@PathVariable Long answerId, @RequestParam Long qnaId, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        answerService.delete(answerId, admin);
        return "redirect:/qna/" + qnaId;
    }
}