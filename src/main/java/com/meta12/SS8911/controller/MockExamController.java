package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.MockExam;
import com.meta12.SS8911.service.MockExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/practice/mock")
@RequiredArgsConstructor
public class MockExamController {

    private final MockExamService mockExamService;

    @GetMapping
    public String list(Model model, Authentication authentication) {
        model.addAttribute("groupedExams", mockExamService.getExamListForUser(authentication.getName()));

        int[] stats = mockExamService.getUserExamStats(authentication.getName());
        model.addAttribute("totalPoolCount", mockExamService.getTotalExamQuestionPoolCount());
        model.addAttribute("attemptCount", stats[0]);
        model.addAttribute("avgRate", stats[1]);

        return "mock/list";
    }

    /**
     * 응시 화면. 실제 문항은 JS가 /api/mock/{examId}/start로 따로 받아감
     * (매번 랜덤으로 다른 문항이 나와야 해서).
     */
    @GetMapping("/play/{examId}")
    public String play(@PathVariable Long examId, Model model) {
        MockExam exam = mockExamService.getExamEntity(examId);

        model.addAttribute("examId", exam.getId());
        model.addAttribute("examTitle", exam.getTitle());
        model.addAttribute("categoryName", exam.getCategory().getTitle());
        model.addAttribute("timeLimitMinutes", exam.getTimeLimitMinutes() == null ? 30 : exam.getTimeLimitMinutes());

        return "mock/play";
    }
}