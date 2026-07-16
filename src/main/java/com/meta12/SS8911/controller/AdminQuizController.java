package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.QuizAdminDTO;
import com.meta12.SS8911.entity.Quiz;
import com.meta12.SS8911.entity.QuizBox;
import com.meta12.SS8911.entity.MockExam;
import com.meta12.SS8911.entity.MockExamBox;
import com.meta12.SS8911.repository.ContentRepository;
import com.meta12.SS8911.repository.QuizRepository;
import com.meta12.SS8911.repository.QuizBoxRepository;
import com.meta12.SS8911.repository.QuizQuestionRepository;
import com.meta12.SS8911.repository.MockExamRepository;
import com.meta12.SS8911.repository.MockExamBoxRepository;
import com.meta12.SS8911.repository.MockExamQuestionRepository;
import com.meta12.SS8911.service.QuizService;
import com.meta12.SS8911.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/quiz")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizService quizService;
    private final ContentService contentService;
    private final ContentRepository contentRepository;

    private final QuizRepository quizRepository;
    private final QuizBoxRepository quizBoxRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final MockExamRepository mockExamRepository;
    private final MockExamBoxRepository mockExamBoxRepository;
    private final MockExamQuestionRepository mockExamQuestionRepository;

    // 목록 (테마/카테고리별로 그룹핑)
    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "연습퀴즈 관리");

        List<Quiz> allQuizzes = quizRepository.findAll();
        // 커리큘럼 테마 순서(K-POP → K-DRAMA → 일상회화 → 심화)에 맞춰 카테고리 id 기준 정렬
        allQuizzes.sort(Comparator.comparing(q -> q.getContent().getCategory().getId()));

        java.util.LinkedHashMap<String, List<QuizAdminDTO>> grouped = new java.util.LinkedHashMap<>();
        for (Quiz quiz : allQuizzes) {
            String categoryName = quiz.getContent().getCategory().getTitle();
            grouped.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(QuizAdminDTO.from(quiz));
        }

        model.addAttribute("groupedQuizzes", grouped);
        return "quiz/admin/list";
    }

    // 등록 화면
    @GetMapping("/chuga")
    public String chuga(Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "연습퀴즈 등록");
        model.addAttribute("groupedContents", contentService.getAllContentGroupedByCategory());
        return "quiz/admin/chuga";
    }

    // 수정 화면
    @GetMapping("/sujung/{id}")
    public String sujung(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "연습퀴즈 수정");
        model.addAttribute("quiz", quizService.getForAdmin(id));
        model.addAttribute("groupedContents", contentService.getAllContentGroupedByCategory());
        return "quiz/admin/sujung";
    }

    // 상세보기 (정답 포함, 관리자만)
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "연습퀴즈 상세보기");
        QuizAdminDTO quiz = quizService.getForAdmin(id);
        model.addAttribute("quiz", quiz);
        return "quiz/admin/view";
    }

    // ==========================================
    // 퀴즈 통계 (연습퀴즈 / 실전모의고사)
    // ==========================================
    @GetMapping("/stats")
    public String stats(Model model, @RequestParam(value = "tab", defaultValue = "practice") String tab) {
        model.addAttribute("activeMenu", "quiz-stats");
        model.addAttribute("pageTitle", "퀴즈 통계");

        if ("mock".equals(tab)) {
            model.addAttribute("currentTab", "mock");
            buildMockExamStats(model);
        } else {
            model.addAttribute("currentTab", "practice");
            buildQuizStats(model);
        }

        return "admin/quiz";
    }

    // ── 연습퀴즈 통계 ──
    private void buildQuizStats(Model model) {
        // ★ 강의 시청 페이지에 임베드된 퀴즈(랜덤 출제, 따로 저장됨)는 연습퀴즈 통계에서 제외
        List<QuizBox> allBoxes = quizBoxRepository.findAll().stream()
                .filter(b -> !b.isEmbedded())
                .collect(Collectors.toList());
        List<Quiz> allQuizzes = quizRepository.findAll();

        long totalAttempts = allBoxes.size();
        long participantCount = allBoxes.stream()
                .filter(b -> b.getUser() != null)
                .map(b -> b.getUser().getId())
                .distinct().count();
        double avgAccuracy = allBoxes.stream()
                .filter(b -> b.getTotal() > 0)
                .mapToDouble(b -> b.getScore() * 100.0 / b.getTotal())
                .average().orElse(0);

        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("participantCount", participantCount);
        model.addAttribute("avgAccuracy", Math.round(avgAccuracy));
        model.addAttribute("setCount", allQuizzes.size());
        model.addAttribute("questionPoolCount", quizQuestionRepository.count());

        model.addAttribute("dailyStats", buildDailyStats(allBoxes.stream()
                .map(QuizBox::getSolvedDate).collect(Collectors.toList())));

        List<SetStatDTO> setStats = allQuizzes.stream().map(q -> {
                    List<QuizBox> forQuiz = allBoxes.stream()
                            .filter(b -> b.getQuiz() != null && b.getQuiz().getId().equals(q.getId()))
                            .collect(Collectors.toList());
                    double avg = forQuiz.stream().filter(b -> b.getTotal() > 0)
                            .mapToDouble(b -> b.getScore() * 100.0 / b.getTotal()).average().orElse(0);
                    String categoryName = (q.getContent() != null && q.getContent().getCategory() != null)
                            ? q.getContent().getCategory().getTitle() : "-";
                    return new SetStatDTO(q.getId(), q.getTitle(), categoryName,
                            q.getQuestions().size(), forQuiz.size(), Math.round(avg));
                }).sorted(Comparator.comparingLong(SetStatDTO::getAttempts).reversed())
                .collect(Collectors.toList());
        model.addAttribute("setStats", setStats);

        Map<String, List<QuizBox>> byCategory = allBoxes.stream()
                .filter(b -> b.getQuiz() != null && b.getQuiz().getContent() != null
                        && b.getQuiz().getContent().getCategory() != null)
                .collect(Collectors.groupingBy(b -> b.getQuiz().getContent().getCategory().getTitle()));

        List<CategoryQuizStatDTO> categoryStats = byCategory.entrySet().stream().map(e -> {
                    double avg = e.getValue().stream().filter(b -> b.getTotal() > 0)
                            .mapToDouble(b -> b.getScore() * 100.0 / b.getTotal()).average().orElse(0);
                    return new CategoryQuizStatDTO(e.getKey(), e.getValue().size(), Math.round(avg));
                }).sorted(Comparator.comparingLong(CategoryQuizStatDTO::getAttempts).reversed())
                .collect(Collectors.toList());
        model.addAttribute("categoryQuizStats", categoryStats);
    }

    // ── 실전 모의고사 통계 ──
    private void buildMockExamStats(Model model) {
        List<MockExamBox> allBoxes = mockExamBoxRepository.findAll();
        List<MockExam> allExams = mockExamRepository.findAll();

        long totalAttempts = allBoxes.size();
        long participantCount = allBoxes.stream()
                .filter(b -> b.getUser() != null)
                .map(b -> b.getUser().getId())
                .distinct().count();
        double avgAccuracy = allBoxes.stream()
                .filter(b -> b.getTotal() > 0)
                .mapToDouble(b -> b.getScore() * 100.0 / b.getTotal())
                .average().orElse(0);

        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("participantCount", participantCount);
        model.addAttribute("avgAccuracy", Math.round(avgAccuracy));
        model.addAttribute("setCount", allExams.size());
        model.addAttribute("questionPoolCount", mockExamQuestionRepository.count());

        model.addAttribute("dailyStats", buildDailyStats(allBoxes.stream()
                .map(MockExamBox::getSolvedDate).collect(Collectors.toList())));

        List<SetStatDTO> setStats = allExams.stream().map(exam -> {
                    List<MockExamBox> forExam = allBoxes.stream()
                            .filter(b -> b.getMockExam() != null && b.getMockExam().getId().equals(exam.getId()))
                            .collect(Collectors.toList());
                    double avg = forExam.stream().filter(b -> b.getTotal() > 0)
                            .mapToDouble(b -> b.getScore() * 100.0 / b.getTotal()).average().orElse(0);
                    String categoryName = exam.getCategory() != null ? exam.getCategory().getTitle() : "-";
                    String title = exam.getTitle() + " (" + exam.getRound() + "회차)";
                    return new SetStatDTO(exam.getId(), title, categoryName,
                            exam.getQuestions().size(), forExam.size(), Math.round(avg));
                }).sorted(Comparator.comparingLong(SetStatDTO::getAttempts).reversed())
                .collect(Collectors.toList());
        model.addAttribute("setStats", setStats);

        Map<String, List<MockExamBox>> byCategory = allBoxes.stream()
                .filter(b -> b.getMockExam() != null && b.getMockExam().getCategory() != null)
                .collect(Collectors.groupingBy(b -> b.getMockExam().getCategory().getTitle()));

        List<CategoryQuizStatDTO> categoryStats = byCategory.entrySet().stream().map(e -> {
                    double avg = e.getValue().stream().filter(b -> b.getTotal() > 0)
                            .mapToDouble(b -> b.getScore() * 100.0 / b.getTotal()).average().orElse(0);
                    return new CategoryQuizStatDTO(e.getKey(), e.getValue().size(), Math.round(avg));
                }).sorted(Comparator.comparingLong(CategoryQuizStatDTO::getAttempts).reversed())
                .collect(Collectors.toList());
        model.addAttribute("categoryQuizStats", categoryStats);
    }

    // ── 공통: 최근 7일 응시 추이 ──
    private List<DailyStatDTO> buildDailyStats(List<LocalDateTime> solvedDates) {
        LocalDate today = LocalDate.now();
        List<Integer> rawCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = solvedDates.stream()
                    .filter(d -> d != null && d.toLocalDate().equals(day))
                    .count();
            rawCounts.add((int) count);
        }
        int maxCount = Math.max(1, Collections.max(rawCounts));
        List<DailyStatDTO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int count = rawCounts.get(6 - i);
            int heightPx = count == 0 ? 4 : Math.max(8, (int) ((count / (double) maxCount) * 110));
            result.add(new DailyStatDTO(day.getMonthValue() + "/" + day.getDayOfMonth(), count, heightPx, "var(--mint-mid)"));
        }
        return result;
    }

    // ── 통계 전용 DTO ──
    public static class DailyStatDTO {
        private final String label; private final int count; private final int heightPx; private final String color;
        public DailyStatDTO(String label, int count, int heightPx, String color) {
            this.label = label; this.count = count; this.heightPx = heightPx; this.color = color;
        }
        public String getLabel() { return label; } public int getCount() { return count; }
        public int getHeightPx() { return heightPx; } public String getColor() { return color; }
    }

    public static class SetStatDTO {
        private final Long id; private final String title; private final String categoryName;
        private final int questionCount; private final long attempts; private final long avgAccuracy;
        public SetStatDTO(Long id, String title, String categoryName, int questionCount, long attempts, long avgAccuracy) {
            this.id = id; this.title = title; this.categoryName = categoryName;
            this.questionCount = questionCount; this.attempts = attempts; this.avgAccuracy = avgAccuracy;
        }
        public Long getId() { return id; } public String getTitle() { return title; }
        public String getCategoryName() { return categoryName; } public int getQuestionCount() { return questionCount; }
        public long getAttempts() { return attempts; } public long getAvgAccuracy() { return avgAccuracy; }
    }

    public static class CategoryQuizStatDTO {
        private final String label; private final long attempts; private final long avgAccuracy;
        public CategoryQuizStatDTO(String label, long attempts, long avgAccuracy) {
            this.label = label; this.attempts = attempts; this.avgAccuracy = avgAccuracy;
        }
        public String getLabel() { return label; } public long getAttempts() { return attempts; }
        public long getAvgAccuracy() { return avgAccuracy; }
    }
}