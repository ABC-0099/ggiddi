package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.EventCreateRequestDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final QnaService qnaService;
    private final SiteUserService siteUserService;
    private final AdminContentService adminContentService;
    private final EventService eventService;

    @GetMapping("")
    public String adminHome() {
        return "redirect:/admin/stats";
    }

    @ModelAttribute("pendingQnaCount")
    public int pendingQnaCount() {
        return (int) qnaService.countPending();
    }

    // ==========================================
    // 통계 대시보드
    // ==========================================
    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("activeMenu", "stats");
        model.addAttribute("pageTitle", "통계 대시보드");

        model.addAttribute("totalContentCount", adminContentService.getAllAdminContents().size());
        // TODO: 전체 회원수 / 이번 달 매출은 실제 서비스 연결되면 채우기
        // model.addAttribute("totalUserCount", siteUserService.getAllUsers().size());
        // model.addAttribute("monthlyRevenue", ...);

        return "admin/stats";
    }

    // ==========================================
    // 콘텐츠 관리
    // ==========================================
    @GetMapping("/content")
    public String content(Model model) {
        model.addAttribute("activeMenu", "content");
        model.addAttribute("pageTitle", "콘텐츠 관리");

        List<AdminContent> realAdminContents = adminContentService.getAllAdminContents();
        List<AdminContentDto> wrappedContentList = new ArrayList<>();
        for (AdminContent ac : realAdminContents) {
            String dateStr = (ac.getCreatedDate() != null) ? ac.getCreatedDate().toLocalDate().toString() : "-";
            wrappedContentList.add(new AdminContentDto(ac.getId(), ac.getTitle(), ac.getStep(), ac.getLectureCount(), dateStr, ac.getStatus()));
        }
        model.addAttribute("contents", wrappedContentList);
        model.addAttribute("totalContentCount", wrappedContentList.size());

        return "admin/content";
    }

    @PostMapping("/content/create")
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

        return "redirect:/admin/content";
    }

    @PostMapping("/content/update/{id}")
    public String updateContent(@PathVariable("id") Long id,
                                @RequestParam("title") String title,
                                @RequestParam("step") String step,
                                @RequestParam("lectureCount") Integer lectureCount,
                                @RequestParam("status") String status) {

        adminContentService.updateContent(id, title, step, lectureCount, status);
        return "redirect:/admin/content";
    }

    @GetMapping("/content/delete/{id}")
    public String deleteContent(@PathVariable("id") Long id) {
        adminContentService.deleteContent(id);
        return "redirect:/admin/content";
    }

    // ==========================================
    // 회원 관리
    // ==========================================
    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("activeMenu", "members");
        model.addAttribute("pageTitle", "회원 관리");
        model.addAttribute("users", siteUserService.getAllUsers());
        return "admin/members";
    }

    // ==========================================
    // 게시판 관리 (Q&A)
    // ==========================================
    @GetMapping("/board")
    public String board(Model model,
                        @RequestParam(value = "category", defaultValue = "all") String category,
                        @RequestParam(value = "kw", defaultValue = "") String kw,
                        @RequestParam(value = "page", defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "board");
        model.addAttribute("pageTitle", "게시판 관리");
        model.addAttribute("currentCategory", category);
        model.addAttribute("kw", kw);

        // 한 페이지당 10개씩 조회
        Page<Qna> realQnas = qnaService.getAdminBoardList(
                category,
                kw,
                PageRequest.of(page, 10)
        );

        List<QnaWrapper> wrappedList = new ArrayList<>();

        for (Qna q : realQnas.getContent()) {

            String authorName = (q.getAuthor() != null)
                    ? q.getAuthor().getUsername()
                    : "알 수 없음";

            String statusStr = (q.getStatus() != null)
                    ? q.getStatus().name()
                    : "PENDING";

            String dateStr = (q.getCreatedDate() != null)
                    ? q.getCreatedDate().toLocalDate().toString()
                    : "2026-07-08";

            wrappedList.add(
                    new QnaWrapper(
                            q.getId(),
                            q.getTitle(),
                            q.getContent(),
                            authorName,
                            dateStr,
                            statusStr
                    )
            );
        }

        Page<QnaWrapper> qnas = new PageImpl<>(
                wrappedList,
                realQnas.getPageable(),
                realQnas.getTotalElements()
        );

        model.addAttribute("qnas", qnas);

        // 페이지네이션용 데이터
        model.addAttribute("currentPage", qnas.getNumber());
        model.addAttribute("totalPages", qnas.getTotalPages());

        return "admin/board";
    }

    // ==========================================
    // 이벤트 관리 (현재는 목데이터 유지)
    // ==========================================
    @GetMapping("/event")
    public String event(Model model) {

        model.addAttribute("activeMenu", "event");
        model.addAttribute("pageTitle", "이벤트 관리");


        // DB 이벤트 목록 조회
        List<Event> eventList = eventService.findAll();

        model.addAttribute("events", eventList);


        // 통계 (임시)
        model.addAttribute("ongoingEventCount", 2);
        model.addAttribute("upcomingEventCount", 1);
        model.addAttribute("totalEventParticipants", 386);
        model.addAttribute("endedEventCount", 5);
        model.addAttribute("totalEventCount", eventList.size());


        System.out.println("eventList : " + eventList);


        return "admin/event";
    }

    @GetMapping("/event/create")
    public String eventCreateForm(Model model) {
        model.addAttribute("activeMenu", "event");
        model.addAttribute("pageTitle", "이벤트 등록");
        return "admin/event-create";
    }

    // TODO: 이벤트 등록 저장 로직이 아직 없어서 폼만 있고 실제 저장 처리가 빠져있습니다.
    // 이벤트 엔티티/서비스가 준비되면 아래처럼 추가하면 됩니다.
    //
    // 이벤트 등록 처리
    @PostMapping("/event/create")
    public String eventCreateSubmit(
            @ModelAttribute EventCreateRequestDto  eventDto,
            @RequestParam(value = "posterFile", required = false) MultipartFile  posterFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
    ) {
        System.out.println("eventDto :" + eventDto);
        eventService.create(eventDto, posterFile, thumbnailFile);

        return "redirect:/admin/event";
    }

    // 이벤트 수정 페이지 이동
    @GetMapping("/event/edit/{id}")
    public String eventEditPage(
            @PathVariable Long id,
            Model model
    ) {

        Event event = eventService.findById(id);

        model.addAttribute("event", event);

        return "admin/eventEdit";
    }


    // 이벤트 수정 처리
    @PostMapping("/edit/{id}")
    public String eventEditSubmit(
            @PathVariable Long id,
            @ModelAttribute EventCreateRequestDto dto,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
    ) {

        eventService.update(
                id,
                dto,
                posterFile,
                thumbnailFile
        );

        return "redirect:/admin/event";
    }

    // ==========================================
    // 결제/수강 관리 (백엔드 연동 전)
    // ==========================================
    @GetMapping("/payment")
    public String payment(Model model) {
        model.addAttribute("activeMenu", "payment");
        model.addAttribute("pageTitle", "결제/수강 관리");
        return "admin/payment";
    }

    // ==========================================
    // 사이트 설정 (백엔드 연동 전)
    // ==========================================
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "사이트 설정");
        return "admin/settings";
    }

    // ── 가짜/DTO 내부 클래스 구조들 (유지) ──
    public static class QnaWrapper {
        private final Long id; private final String title; private final String content; private final AuthorMock author; private final String createdDate; private final String status;
        public QnaWrapper(Long id, String title, String content, String username, String createdDate, String status) { this.id = id; this.title = title; this.content = content; this.author = new AuthorMock(username); this.createdDate = createdDate; this.status = status; }
        public Long getId() { return id; } public String getTitle() { return title; } public String getContent() { return content; } public AuthorMock getAuthor() { return author; } public String getCreatedDate() { return createdDate; } public String getStatus() { return status; }
    }
    public static class AuthorMock { private final String username; public AuthorMock(String username) { this.username = username; } public String getUsername() { return username; } }
    public static class AdminContentDto {
        private final Long id; private final String title; private final String step; private final int lectureCount; private final String createdDate; private final String status;
        public AdminContentDto(Long id, String title, String step, int lectureCount, String createdDate, String status) { this.id = id; this.title = title; this.step = step; this.lectureCount = lectureCount; this.createdDate = createdDate; this.status = status; }
        public Long getId() { return id; } public String getTitle() { return title; } public String getStep() { return step; } public int getLectureCount() { return lectureCount; } public String getCreatedDate() { return createdDate; } public String getStatus() { return status; }
    }
    public static class EventMock {
        private final String title; private final String startDate; private final String endDate; private final int participantCount; private final String createdDate; private final String status;
        public EventMock(String title, String startDate, String endDate, int participantCount, String createdDate, String status) { this.title = title; this.startDate = startDate; this.endDate = endDate; this.participantCount = participantCount; this.createdDate = createdDate; this.status = status; }
        public String getTitle() { return title; } public String getStartDate() { return startDate; } public String getEndDate() { return endDate; } public int getParticipantCount() { return participantCount; } public String getCreatedDate() { return createdDate; } public String getStatus() { return status; }
    }
}