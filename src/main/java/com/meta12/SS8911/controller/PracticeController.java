package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.MockExamBox;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.MockExamBoxRepository;
import com.meta12.SS8911.repository.QuizRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PracticeController {

    private final MockExamBoxRepository mockExamBoxRepository;
    private final SiteUserRepository siteUserRepository;
    private final CategoryService categoryService;
    private final QuizRepository quizRepository;

    /**
     * 배움터 메인 (연습퀴즈 카테고리 목록 + 실전모의고사 최근 응시 이력).
     */
    @GetMapping("/practice/main")
    public String main(Model model, Principal principal) {

        // ── 연습퀴즈 카테고리 목록 + 단원별 문항 수 ──
        List<Category> categoryList = categoryService.findAll();

        List<Map<String, Object>> categories = categoryList.stream()
                .map(category -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", category.getId());
                    m.put("name", category.getTitle());
                    long quizCount = quizRepository.countQuestionsByCategoryId(category.getId());
                    m.put("quizCount", quizCount);
                    return m;
                })
                .collect(Collectors.toList());

        model.addAttribute("categories", categories);

        // ── 실전 모의고사: 최근 응시 이력 + 평균 정답률 ──
        if (principal != null) {
            SiteUser user = siteUserRepository.findByUsername(principal.getName()).orElse(null);

            if (user != null) {
                List<MockExamBox> boxes = mockExamBoxRepository.findByUserIdOrderBySolvedDateDesc(user.getId());

                List<Map<String, Object>> recentExams = boxes.stream()
                        .limit(5)
                        .map(box -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("title", box.getMockExam().getTitle());
                            m.put("date", box.getSolvedDate().toLocalDate().toString());
                            int percent = box.getTotal() > 0
                                    ? (int) Math.round(box.getScore() * 100.0 / box.getTotal())
                                    : 0;
                            m.put("score", percent);
                            return m;
                        })
                        .collect(Collectors.toList());

                model.addAttribute("recentExams", recentExams);

                Double avgAccuracy = mockExamBoxRepository.findAvgAccuracyByUserId(user.getId());
                int avgAccuracyPercent = (int) Math.round((avgAccuracy != null ? avgAccuracy : 0) * 100);
                model.addAttribute("avgAccuracy", avgAccuracyPercent + "%");
            }
        }

        return "practice/main";
    }
}