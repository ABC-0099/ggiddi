package com.meta12.SS8911.controller;


import com.meta12.SS8911.dto.EventCreateRequestDto;
import com.meta12.SS8911.entity.AdminContent;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.config.OrderPayStatus;
import com.meta12.SS8911.entity.AdminContent;
import com.meta12.SS8911.entity.OrderPay;

import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.repository.OrderPayRepository;
import com.meta12.SS8911.service.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.meta12.SS8911.entity.SiteUser;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final QnaService qnaService;
    private final SiteUserService siteUserService;
    private final AdminContentService adminContentService;

    private final EventService eventService;

    private final OrderPayService orderPayService;
    private final OrderPayRepository orderPayRepository; // payment 페이지 페이징 조회 전용 (Repository/Service 파일 자체는 미수정)


    /**
     * 사이드바 뱃지 + 탑바 알림 점 표시용.
     * 모든 /admin/** 페이지에서 자동으로 model에 채워짐.
     */
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

        // ── KPI: 전체 회원, 전체 콘텐츠 ──
        List<SiteUser> allUsers = siteUserService.getAllUsers();
        long activeUserCount = allUsers.stream().filter(u -> !u.isWithdrawn()).count();
        model.addAttribute("totalUserCount", activeUserCount);
        model.addAttribute("totalContentCount", adminContentService.getAllAdminContents().size());

        // ── KPI: 이번 달 신규가입 ──
        YearMonth thisMonth = YearMonth.now();
        long newSignupsThisMonth = allUsers.stream()
                .filter(u -> u.getJoinDate() != null)
                .filter(u -> YearMonth.from(u.getJoinDate()).equals(thisMonth))
                .count();
        model.addAttribute("newSignupsThisMonth", newSignupsThisMonth);

        // ── KPI: 이번 달 매출 (성공 결제만) ──
        List<OrderPay> allOrders = orderPayService.listAll();
        long monthlyRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderPayStatus.SUCCESS)
                .filter(o -> o.getPayday() != null && YearMonth.from(o.getPayday()).equals(thisMonth))
                .mapToLong(this::parsePrice)
                .sum();
        model.addAttribute("monthlyRevenue", monthlyRevenue);

        // ── 막대 차트: 월별 신규 가입자 수 (최근 6개월) ──
        List<MonthlyStat> monthlyStats = new ArrayList<>();
        String[] barColors = {"var(--mint-light)", "var(--mint-light)", "var(--mint-light)",
                "var(--mint-mid)", "var(--mint-mid)", "var(--navy)"};
        List<Integer> rawCounts = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = thisMonth.minusMonths(i);
            long count = allUsers.stream()
                    .filter(u -> u.getJoinDate() != null)
                    .filter(u -> YearMonth.from(u.getJoinDate()).equals(ym))
                    .count();
            rawCounts.add((int) count);
        }
        int maxCount = Math.max(1, Collections.max(rawCounts));
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = thisMonth.minusMonths(i);
            int count = rawCounts.get(5 - i);
            int heightPx = count == 0 ? 4 : Math.max(8, (int) ((count / (double) maxCount) * 110));
            monthlyStats.add(new MonthlyStat(ym.getMonthValue() + "월", count, heightPx, barColors[5 - i]));
        }
        model.addAttribute("monthlyStats", monthlyStats);
        model.addAttribute("cumulativeUserCount", activeUserCount);

        // ── 도넛 차트: 카테고리별 결제 비율 (성공 결제 기준) ──
        List<OrderPay> successOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderPayStatus.SUCCESS)
                .collect(Collectors.toList());

        Map<String, Long> categoryCounts = successOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> (o.getCategory() != null && o.getCategory().getTitle() != null) ? o.getCategory().getTitle() : "미분류",
                        Collectors.counting()));

        List<CategoryStat> categoryStats = new ArrayList<>();
        String donutGradient = null;
        long totalSuccessCount = successOrders.size();

        if (totalSuccessCount > 0) {
            String[] donutColors = {"#185FA5", "#5DCAA5", "#9FE1CB", "#EF9F27", "#D85A30", "#E6F1FB"};
            List<Map.Entry<String, Long>> sorted = categoryCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList());

            StringBuilder gradient = new StringBuilder("conic-gradient(");
            double cursor = 0;
            int colorIdx = 0;
            for (Map.Entry<String, Long> e : sorted) {
                double percent = (e.getValue() * 100.0) / totalSuccessCount;
                String color = donutColors[colorIdx % donutColors.length];
                gradient.append(color).append(" ").append(cursor).append("% ").append(cursor + percent).append("%, ");
                categoryStats.add(new CategoryStat(e.getKey(), e.getValue(), Math.round(percent), color));
                cursor += percent;
                colorIdx++;
            }
            gradient.setLength(gradient.length() - 2); // 마지막 ", " 제거
            gradient.append(")");
            donutGradient = gradient.toString();
        }
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("donutGradient", donutGradient);

        // ── 최근 결제 내역 5건 ──
        List<OrderPay> recentPayments = allOrders.stream()
                .filter(o -> o.getPayday() != null)
                .sorted((a, b) -> b.getPayday().compareTo(a.getPayday()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentPayments", recentPayments);

        return "admin/stats";
    }

    // price(String)를 숫자로 안전 변환하는 헬퍼
    private long parsePrice(OrderPay order) {
        try {
            return Long.parseLong(order.getPrice().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0L;
        }
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
    // 게시판 관리 (Q&A) — "전체" 탭 + 페이지네이션 반영
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

        Page<Qna> realQnas = qnaService.getAdminBoardList(category, kw, PageRequest.of(page, 10));

        List<QnaWrapper> wrappedList = new ArrayList<>();
        for (Qna q : realQnas.getContent()) {
            String authorName = (q.getAuthor() != null) ? q.getAuthor().getUsername() : "알 수 없음";
            String statusStr = (q.getStatus() != null) ? q.getStatus().name() : "PENDING";
            String dateStr = (q.getCreatedDate() != null) ? q.getCreatedDate().toLocalDate().toString() : "2026-07-08";

            wrappedList.add(new QnaWrapper(q.getId(), q.getTitle(), q.getContent(), authorName, dateStr, statusStr));
        }
        Page<QnaWrapper> qnas = new PageImpl<>(wrappedList, realQnas.getPageable(), realQnas.getTotalElements());
        model.addAttribute("qnas", qnas);

        return "admin/board";
    }

    // ==========================================
    // 이벤트 관리 (현재는 목데이터만 - 보여주기 전용)
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

    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/stats";
    }

    // TODO: 이벤트 실제 저장 로직은 아직 없음 (지금은 화면만 보여주기로 결정됨)


    // ==========================================
    // 결제/수강 관리 — OrderPay 실데이터 연동
    // (OrderPayRepository/OrderPayService 파일 자체는 미수정 - 이미 있는 기본/기존 메서드만 사용)
    // ==========================================
    @GetMapping("/payment")
    public String payment(Model model,
                          @RequestParam(value = "page", defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "payment");
        model.addAttribute("pageTitle", "결제/수강 관리");

        // JpaRepository가 기본 제공하는 findAll(Pageable) / count() 사용
        Page<OrderPay> orders = orderPayRepository.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "payday"))
        );
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrderCount", orderPayRepository.count());

        // KPI 집계는 기존 OrderPayService.listAll() 사용
        List<OrderPay> allOrders = orderPayService.listAll();

        long successCount = allOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.SUCCESS).count();
        long cancelCount = allOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.CANCEL).count();
        long failedCount = allOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.FAILED).count();

        long totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderPayStatus.SUCCESS)
                .mapToLong(o -> {
                    try {
                        return Long.parseLong(o.getPrice().replaceAll("[^0-9]", ""));
                    } catch (Exception e) {
                        return 0L;
                    }
                })
                .sum();

        model.addAttribute("successCount", successCount);
        model.addAttribute("cancelCount", cancelCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("totalRevenue", totalRevenue);

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

    public static class MonthlyStat {
        private final String label; private final int count; private final int heightPx; private final String color;
        public MonthlyStat(String label, int count, int heightPx, String color) { this.label = label; this.count = count; this.heightPx = heightPx; this.color = color; }
        public String getLabel() { return label; } public int getCount() { return count; } public int getHeightPx() { return heightPx; } public String getColor() { return color; }
    }
    public static class CategoryStat {
        private final String label; private final long count; private final long percent; private final String color;
        public CategoryStat(String label, long count, long percent, String color) { this.label = label; this.count = count; this.percent = percent; this.color = color; }
        public String getLabel() { return label; } public long getCount() { return count; } public long getPercent() { return percent; } public String getColor() { return color; }
    }
}