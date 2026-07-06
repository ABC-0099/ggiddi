package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.AnswerDTO;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.AnswerService;
import com.meta12.SS8911.service.SiteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final SiteUserService siteUserService;

    // 답변 작성 (관리자 마이페이지/게시판 관리 모달에서 특정 문의에 답변)
    @PostMapping("/qna/{qnaId}/answer")
    public String create(@PathVariable Long qnaId, @Valid AnswerDTO dto,
                         BindingResult bindingResult,
                         @RequestParam(required = false) String returnTo,
                         Principal principal) {
        String fallback = "redirect:/qna/" + qnaId;
        if (bindingResult.hasErrors()) {
            return fallback;
        }

        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        answerService.create(qnaId, dto.getContent(), admin);

        return (returnTo != null && !returnTo.isBlank()) ? "redirect:" + returnTo : fallback;
    }

    // 답변 수정
    @PostMapping("/answer/{answerId}/edit")
    public String edit(@PathVariable Long answerId, @Valid AnswerDTO dto,
                       BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/qna/" + dto.getQnaId();
        }

        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        answerService.update(answerId, dto.getContent(), admin);
        return "redirect:/qna/" + dto.getQnaId();
    }

    // 답변 삭제
    @PostMapping("/answer/{answerId}/delete")
    public String delete(@PathVariable Long answerId, @RequestParam Long qnaId, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        answerService.delete(answerId, admin);
        return "redirect:/qna/" + qnaId;
    }
}