package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.QnaDTO;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final SiteUserService siteUserService;

    @GetMapping("/main")
    public String main() {
        return "qna/main";
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("qnaDTO", new QnaDTO());
        return "qna/write";
    }

    @PostMapping("/write")
    public String write(QnaDTO dto, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        qnaService.create(dto, user);
        return "redirect:/qna/main";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Qna qna = qnaService.getQna(id);
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        qnaService.checkViewPermission(qna, user);

        model.addAttribute("qna", qna);
        return "qna/view";
    }

    @PostMapping("/{id}/answer")
    public String answer(@PathVariable Long id, @RequestParam String answer, Principal principal) {
        SiteUser admin = siteUserService.getUserByUsername(principal.getName());
        qnaService.answer(id, answer, admin);
        return "redirect:/qna/" + id;
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Qna qna = qnaService.getQna(id);
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        qnaService.checkEditDeletePermission(qna, user);

        QnaDTO dto = new QnaDTO();
        dto.setTitle(qna.getTitle());
        dto.setContent(qna.getContent());
        dto.setCategory(qna.getCategory());
        dto.setExistingImages(qna.getFiles());

        model.addAttribute("qnaDTO", dto);
        model.addAttribute("qnaId", id);
        return "qna/edit";
    }

    // 수정 처리
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, QnaDTO dto, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        qnaService.update(id, dto, user);
        return "redirect:/qna/" + id;
    }

    // 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        qnaService.delete(id, user);
        return "redirect:/siteUser/mypage?tab=posts&subtab=myinquiry";
    }
}