package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Quiz;
import com.meta12.SS8911.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 연습퀴즈 목록. categoryId로 필터링 가능 (배움터 메인 카테고리 카드에서 진입).
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long categoryId,
                       Model model, Authentication authentication) {
        model.addAttribute("groupedQuizzes", quizService.getQuizListForUser(authentication.getName(), categoryId));

        int[] stats = quizService.getUserQuizStats(authentication.getName());
        model.addAttribute("totalPoolCount", quizService.getTotalQuestionPoolCount());
        model.addAttribute("attemptCount", stats[0]);
        model.addAttribute("avgRate", stats[1]);

        return "quiz/list";
    }

    /**
     * 퀴즈 풀이 화면. 실제 문항 데이터는 JS가 /api/quiz/{quizId}/start로 따로 받아감
     * (매번 랜덤으로 다른 문항이 나와야 해서 페이지 로드 시점에 미리 안 심어둠).
     */
    @GetMapping("/play/{quizId}")
    public String play(@PathVariable Long quizId, Model model, Authentication authentication) {
        Quiz quiz = quizService.getQuizEntity(quizId);

        model.addAttribute("quizId", quiz.getId());
        model.addAttribute("quizTitle", quiz.getTitle());
        model.addAttribute("contentTitle", quiz.getContent().getTitle());

        return "quiz/play";
    }
}