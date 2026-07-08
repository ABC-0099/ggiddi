package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.AdminContent;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.service.AdminContentService;
import com.meta12.SS8911.service.EventService;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor // 💡 private final로 선언된 모든 객체의 생성자 주입을 자동으로 처리합니다.
public class AdminController {

    private final QnaService qnaService;
    private final SiteUserService siteUserService;
    private final AdminContentService adminContentService; // 💡 필드 단일화로 주입 충돌 방지
    private final EventService eventService;

    @GetMapping("/admin")
    public String adminDashboard(Model model,
                                 @RequestParam(value = "category", defaultValue = "notice") String category,
                                 @RequestParam(value = "kw", defaultValue = "") String kw,
                                 @RequestParam(value = "panel", defaultValue = "stats") String panel) {

        // 브라우저 새로고침 시에도 기존에 보던 탭(패널)이 그대로 열려있도록 전달
        model.addAttribute("currentPanel", panel);
        model.addAttribute("currentCategory", category);
        model.addAttribute("users", siteUserService.getAllUsers());
        model.addAttribute("kw", kw);

        // ==========================================
        // 1. Q&A / 게시판 관리 데이터 연동 (진짜 DB)
        // ==========================================
        Page<Qna> realQnas = qnaService.getAdminBoardList(category, kw, PageRequest.of(0, 20));

        List<QnaWrapper> wrappedList = new ArrayList<>();
        for (Qna q : realQnas.getContent()) {
            String authorName = (q.getAuthor() != null) ? q.getAuthor().getUsername() : "알 수 없음";
            String statusStr = (q.getStatus() != null) ? q.getStatus().name() : "PENDING";
            String dateStr = (q.getCreatedDate() != null) ? q.getCreatedDate().toLocalDate().toString() : "2026-07-08";

            wrappedList.add(new QnaWrapper(q.getId(), q.getTitle(), q.getContent(), authorName, dateStr, statusStr));
        }
        Page<QnaWrapper> qnas = new PageImpl<>(wrappedList, realQnas.getPageable(), realQnas.getTotalElements());
        model.addAttribute("qnas", qnas);
        model.addAttribute("pendingQnaCount", qnaService.countPending());

        // ==========================================
        // 2. 콘텐츠 관리 데이터 연동 (진짜 DB)
        // ==========================================
        List<AdminContent> realAdminContents = adminContentService.getAllAdminContents();
        List<AdminContentDto> wrappedContentList = new ArrayList<>();
        for (AdminContent ac : realAdminContents) {
            String dateStr = (ac.getCreatedDate() != null) ? ac.getCreatedDate().toLocalDate().toString() : "-";
            wrappedContentList.add(new AdminContentDto(ac.getId(), ac.getTitle(), ac.getStep(), ac.getLectureCount(), dateStr, ac.getStatus()));
        }
        model.addAttribute("contents", wrappedContentList);
        model.addAttribute("totalContentCount", wrappedContentList.size());
        model.addAttribute("users", siteUserService.getAllUsers());

        // ==========================================
        // 3. 이벤트 가짜 데이터들 (유지)
        // ==========================================
        model.addAttribute("ongoingEventCount", 2);
        model.addAttribute("upcomingEventCount", 1);
        model.addAttribute("totalEventParticipants", 386);
        model.addAttribute("endedEventCount", 5);
        model.addAttribute("totalEventCount", 8);

        List<EventMock> eventList = new ArrayList<>();
        eventList.add(new EventMock("여름맞이 출석 챌린지", "2026.07.01", "2026.07.31", 128, "2026.06.20", "ONGOING"));
        eventList.add(new EventMock("신규가입 웰컴 이벤트", "2026.06.01", "상시", 241, "2026.05.28", "ONGOING"));
        eventList.add(new EventMock("추석맞이 K-문화 퀴즈전", "2026.09.20", "2026.09.27", 0, "2026.06.30", "UPCOMING"));
        model.addAttribute("events", eventList);

        model.addAttribute("events", eventService.findAll());

        return "admin/dashboard";
    }

    // ==========================================
    // 실제 콘텐츠 등록 처리 API
    // ==========================================
    @PostMapping("/admin/content/create")
    public String createContent(@RequestParam String title,
                                @RequestParam String step,
                                @RequestParam int lectureCount,
                                @RequestParam(defaultValue = "공개") String status) {

        AdminContent newContent = new AdminContent();
        newContent.setTitle(title);
        newContent.setStep(step);
        newContent.setLectureCount(lectureCount);
        newContent.setStatus(status);

        adminContentService.saveContent(newContent);

        return "redirect:/admin?panel=content";
    }

    // ==========================================
    // 💡 실제 콘텐츠 수정 처리 API (안전하게 교체됨)
    // ==========================================
    @PostMapping("/admin/content/update/{id}")
    public String updateContent(@PathVariable("id") Long id,
                                @RequestParam("title") String title,
                                @RequestParam("step") String step,
                                @RequestParam("lectureCount") Integer lectureCount,
                                @RequestParam("status") String status) {

        // 정렬된 단일 빈 구조인 adminContentService 인스턴스로 안전하게 위임 호출합니다.
        adminContentService.updateContent(id, title, step, lectureCount, status);
        return "redirect:/admin?panel=content";
    }

    // ==========================================
    // 💡 실제 콘텐츠 삭제 처리 API (안전하게 교체됨)
    // ==========================================
    @GetMapping("/admin/content/delete/{id}")
    public String deleteContent(@PathVariable("id") Long id) {

        adminContentService.deleteContent(id);
        return "redirect:/admin?panel=content";
    }

    // ── 가짜/DTO 내부 클래스 구조들 (유지) ──
    public static class QnaWrapper {
        private final Long id;
        private final String title;
        private final String content;
        private final AuthorMock author;
        private final String createdDate;
        private final String status;

        public QnaWrapper(Long id, String title, String content, String username, String createdDate, String status) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = new AuthorMock(username);
            this.createdDate = createdDate;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public AuthorMock getAuthor() {
            return author;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class AuthorMock {
        private final String username;

        public AuthorMock(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class AdminContentDto {
        private final Long id;
        private final String title;
        private final String step;
        private final int lectureCount;
        private final String createdDate;
        private final String status;

        public AdminContentDto(Long id, String title, String step, int lectureCount, String createdDate, String status) {
            this.id = id;
            this.title = title;
            this.step = step;
            this.lectureCount = lectureCount;
            this.createdDate = createdDate;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getStep() {
            return step;
        }

        public int getLectureCount() {
            return lectureCount;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class EventMock {
        private final String title;
        private final String startDate;
        private final String endDate;
        private final int participantCount;
        private final String createdDate;
        private final String status;

        public EventMock(String title, String startDate, String endDate, int participantCount, String createdDate, String status) {
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.participantCount = participantCount;
            this.createdDate = createdDate;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public int getParticipantCount() {
            return participantCount;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getStatus() {
            return status;
        }
    }


    @PostMapping("/admin/event/create")
    public String createEvent(Event event) {

        eventService.create(event);

        return "redirect:/admin?panel=event";
    }


}