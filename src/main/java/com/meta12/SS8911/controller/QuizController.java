package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Quiz;
import com.meta12.SS8911.entity.QuizBox;
import com.meta12.SS8911.entity.QuizUnit;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.QuizService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final SiteUserService siteUserService;

    // 퀴즈 풀기 화면
    @GetMapping("/{id}")
    public String quiz(@PathVariable Long id, Model model, Principal principal) {
        Quiz quiz = quizService.getQuiz(id);

        // 잠금 여부 체크
        if (!quiz.isUnlocked()) {
            return "redirect:/"; // 잠겨있으면 홈으로
        }

        List<QuizUnit> units = quizService.getUnits(quiz);
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        QuizBox prevResult = quizService.getResult(quiz, user);

        model.addAttribute("quiz", quiz);
        model.addAttribute("units", units);
        model.addAttribute("prevResult", prevResult); // 이전 풀이 결과 (있으면)
        return "quiz/quiz";
    }

    // 퀴즈 제출 및 채점
    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @RequestParam Map<String, String> params,
                         Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());

        // params에서 답안만 추출 (key: "answer_문항id", value: "A"/"B"/"C"/"D")
        Map<Long, String> answers = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("answer_")) {
                Long unitId = Long.parseLong(entry.getKey().replace("answer_", ""));
                answers.put(unitId, entry.getValue());
            }
        }

        QuizBox result = quizService.submit(id, answers, user);
        return "redirect:/quiz/" + id + "/result/" + result.getId();
    }

    // 퀴즈 결과 화면
    @GetMapping("/{id}/result/{resultId}")
    public String result(@PathVariable Long id,
                         @PathVariable Long resultId,
                         Model model) {
        Quiz quiz = quizService.getQuiz(id);
        List<QuizUnit> units = quizService.getUnits(quiz);
        model.addAttribute("quiz", quiz);
        model.addAttribute("units", units);
        model.addAttribute("resultId", resultId);
        return "quiz/result";
    }
}
