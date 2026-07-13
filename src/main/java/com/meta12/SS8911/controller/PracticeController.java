package com.meta12.SS8911.controller;

import com.meta12.SS8911.config.SourceType;
import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.MockExamBox;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.entity.WrongAnswerNote;
import com.meta12.SS8911.repository.MockExamBoxRepository;
import com.meta12.SS8911.repository.QuizRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.repository.WrongAnswerNoteRepository;
import com.meta12.SS8911.service.CategoryService;
import com.meta12.SS8911.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private final QuizService quizService;
    private final WrongAnswerNoteRepository wrongAnswerNoteRepository; // ★ 오답노트 요약용

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

                // ★ 히어로 "완료 퀴즈" - 연습퀴즈(QuizBox) 풀이 횟수
                long completedQuizCount = quizService.countCompletedByUser(user.getId());
                model.addAttribute("completedQuizCount", completedQuizCount);

                // ★ 히어로 "최근 모의고사" - 가장 최근 응시 1건의 점수. 응시 기록이 없으면 null 유지 (fallback 오표시 방지)
                if (!boxes.isEmpty()) {
                    MockExamBox latest = boxes.get(0);
                    int latestPercent = latest.getTotal() > 0
                            ? (int) Math.round(latest.getScore() * 100.0 / latest.getTotal())
                            : 0;
                    model.addAttribute("lastMockScore", latestPercent + "점");
                }

                Double avgAccuracy = mockExamBoxRepository.findAvgAccuracyByUserId(user.getId());
                int avgAccuracyPercent = (int) Math.round((avgAccuracy != null ? avgAccuracy : 0) * 100);
                model.addAttribute("avgAccuracy", avgAccuracyPercent + "%");

                // ★ 오답노트 요약 - 미복습 개수 + 최근 3개 미리보기
                long wrongNoteCount = wrongAnswerNoteRepository.countBySiteUserAndReviewedFalse(user);
                model.addAttribute("wrongNoteCount", wrongNoteCount);

                List<WrongAnswerNote> recentWrongNotes = wrongAnswerNoteRepository
                        .findBySiteUserOrderByCreatedDateDesc(user, PageRequest.of(0, 3))
                        .getContent();
                model.addAttribute("recentWrongNotes", recentWrongNotes);
            }
        }

        return "practice/main";
    }

    /**
     * 오답노트 전체 목록 - 출처(연습퀴즈/모의고사)별 필터링 + 페이지네이션.
     */
    @GetMapping("/practice/wrong-notes")
    public String wrongNotes(@RequestParam(required = false) String sourceType,
                             @RequestParam(defaultValue = "0") int page,
                             Model model, Principal principal) {
        if (principal == null) return "redirect:/siteUser/login";

        SiteUser user = siteUserRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/siteUser/login";

        Pageable pageable = PageRequest.of(page, 10);
        Page<WrongAnswerNote> notes;

        SourceType type = null;
        if (sourceType != null && !sourceType.isBlank()) {
            try {
                type = SourceType.valueOf(sourceType);
            } catch (IllegalArgumentException ignored) {
                // 잘못된 값이면 전체 조회로 폴백
            }
        }

        notes = (type != null)
                ? wrongAnswerNoteRepository.findBySiteUserAndSourceTypeOrderByCreatedDateDesc(user, type, pageable)
                : wrongAnswerNoteRepository.findBySiteUserOrderByCreatedDateDesc(user, pageable);

        model.addAttribute("notes", notes);
        model.addAttribute("selectedSourceType", type);
        model.addAttribute("wrongNoteCount", wrongAnswerNoteRepository.countBySiteUserAndReviewedFalse(user));

        return "practice/wrong-notes";
    }

    /**
     * 오답 하나를 복습완료/미완료로 토글 (체크박스 클릭 시 AJAX 호출).
     */
    @PostMapping("/api/wrong-notes/{id}/review")
    @ResponseBody
    public Map<String, Object> toggleReviewed(@PathVariable Long id, Principal principal) {
        Map<String, Object> result = new HashMap<>();

        if (principal == null) {
            result.put("success", false);
            return result;
        }

        SiteUser user = siteUserRepository.findByUsername(principal.getName()).orElse(null);
        WrongAnswerNote note = wrongAnswerNoteRepository.findById(id).orElse(null);

        // 본인 소유의 오답노트만 토글 가능 (다른 사람 것 조작 방지)
        if (user == null || note == null || note.getSiteUser() == null
                || !note.getSiteUser().getId().equals(user.getId())) {
            result.put("success", false);
            return result;
        }

        note.setReviewed(!note.isReviewed());
        wrongAnswerNoteRepository.save(note);

        result.put("success", true);
        result.put("reviewed", note.isReviewed());
        return result;
    }
}