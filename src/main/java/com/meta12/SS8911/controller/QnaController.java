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
}