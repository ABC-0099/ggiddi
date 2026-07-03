package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.QuizAdminDTO;
import com.meta12.SS8911.repository.ContentRepository;
import com.meta12.SS8911.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/quiz")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizService quizService;
    private final ContentRepository contentRepository;

    // 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("quizzes", quizService.getAllForAdmin());
        return "quiz/admin/list";
    }

    // 등록 화면
    @GetMapping("/chuga")
    public String chuga(Model model) {
        model.addAttribute("contents", contentRepository.findAll());
        return "quiz/admin/chuga";
    }

    // 수정 화면
    @GetMapping("/sujung/{id}")
    public String sujung(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizService.getForAdmin(id));
        model.addAttribute("contents", contentRepository.findAll());
        return "quiz/admin/sujung";
    }

    // 상세보기 (정답 포함, 관리자만)
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        QuizAdminDTO quiz = quizService.getForAdmin(id);
        model.addAttribute("quiz", quiz);
        return "quiz/admin/view";
    }
}