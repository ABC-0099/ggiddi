package com.meta12.SS8911.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PracticeController {

    /**
     * 배움터 메인 (연습퀴즈 카테고리 목록 + 실전모의고사 최근 응시 이력).
     *
     * categories / recentExams를 아직 안 채워도 templates/practice/main.html이
     * null 체크로 프리뷰 카드를 대신 보여주므로 화면 확인엔 문제없음.
     * 실제 카테고리/모의고사 서비스가 준비되면 이 메서드에서 채워서 넘기면 됨.
     */
    @GetMapping("/practice/main")
    public String main(Model model) {
        // model.addAttribute("categories", categoryService.getAllWithQuizCount());
        // model.addAttribute("recentExams", mockExamService.getRecentByUser(authentication));
        return "practice/main";
    }
}