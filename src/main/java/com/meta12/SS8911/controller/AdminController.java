package com.meta12.SS8911.controller;

import com.meta12.SS8911.config.OrderPayStatus;
import com.meta12.SS8911.dto.ContentDTO;
import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.OrderPay;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.OrderPayRepository;
import com.meta12.SS8911.service.CategoryService;
import com.meta12.SS8911.service.ContentService;
import com.meta12.SS8911.service.OrderPayService;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SiteUserService;
import com.meta12.SS8911.dto.CategoryDTO;
import com.meta12.SS8911.entity.Category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final QnaService qnaService;
    private final SiteUserService siteUserService;
    private final ContentService contentService;
    private final CategoryService categoryService;
    private final OrderPayService orderPayService;
    private final OrderPayRepository orderPayRepository; // payment 페이지 페이징 조회 전용 (findAll(Pageable) 내장 메서드만 사용)

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
        model.addAttribute("totalContentCount", contentService.getAllContentList().size());

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

    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/stats";
    }

    // ==========================================
    // 콘텐츠 관리 (Content/Category 실데이터)
    // ==========================================
    @GetMapping("/content")
    public String content(Model model) {
        model.addAttribute("activeMenu", "content");
        model.addAttribute("pageTitle", "콘텐츠 관리");

        List<Content> contents = contentService.getAllContentList();
        model.addAttribute("contents", contents);
        model.addAttribute("totalContentCount", contents.size());

        return "admin/admin_content/content";
    }

    @GetMapping("/content/create")
    public String contentCreateForm(Model model) {
        model.addAttribute("activeMenu", "content");
        model.addAttribute("pageTitle", "콘텐츠 등록");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("contentDTO", new ContentDTO());
        return "admin/admin_content/content-create";
    }

    @PostMapping("/content/create")
    public String contentCreateSubmit(@ModelAttribute ContentDTO contentDTO) {
        contentService.chugaProc(contentDTO);
        return "redirect:/admin/content";
    }

    @GetMapping("/content/edit/{id}")
    public String contentEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "content");
        model.addAttribute("pageTitle", "콘텐츠 수정");
        model.addAttribute("categories", categoryService.findAll());

        Content content = contentService.getContent(id);

        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setVideoUrl(content.getVideoUrl());
        dto.setSequence(content.getSequence());
        dto.setCategoryId(content.getCategory() != null ? content.getCategory().getId() : null);
        dto.setStage(content.getStage());
        dto.setDescription(content.getDescription());
        dto.setKeywords(content.getKeywords());
        dto.setStatus(content.getStatus());
        dto.setPublishAt(content.getPublishAt());
        dto.setFree(content.isFree());

        model.addAttribute("contentDTO", dto);
        model.addAttribute("existingContent", content);
        return "admin/admin_content/content-edit";
    }

    @PostMapping("/content/update/{id}")
    public String contentUpdateSubmit(@PathVariable Long id, @ModelAttribute ContentDTO contentDTO) {
        contentDTO.setId(id);
        contentService.sujungProc(contentDTO);
        return "redirect:/admin/content";
    }

    @GetMapping("/content/delete/{id}")
    public String deleteContent(@PathVariable Long id) {
        contentService.delete(id);
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
    // 게시판 관리 (Q&A) — "전체" 탭 + 페이지네이션
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

        model.addAttribute("ongoingEventCount", 2);
        model.addAttribute("upcomingEventCount", 1);
        model.addAttribute("totalEventParticipants", 386);
        model.addAttribute("endedEventCount", 5);
        model.addAttribute("totalEventCount", 8);

        List<EventMock> eventList = new ArrayList<>();
        eventList.add(new EventMock(1L, "여름맞이 출석 챌린지", "2026.07.01", "2026.07.31", 128, "2026.06.20", "ONGOING"));
        eventList.add(new EventMock(2L, "신규가입 웰컴 이벤트", "2026.06.01", "상시", 241, "2026.05.28", "ONGOING"));
        eventList.add(new EventMock(3L, "추석맞이 K-문화 퀴즈전", "2026.09.20", "2026.09.27", 0, "2026.06.30", "UPCOMING"));
        model.addAttribute("events", eventList);

        return "admin/event";
    }

    @GetMapping("/event/create")
    public String eventCreateForm(Model model) {
        model.addAttribute("activeMenu", "event");
        model.addAttribute("pageTitle", "이벤트 등록");
        return "admin/event-create";
    }

    // TODO: 이벤트 실제 저장 로직은 아직 없음 (지금은 화면만 보여주기로 결정됨)

    // ==========================================
    // 결제/수강 관리 — OrderPay 실데이터 연동
    // (OrderPayRepository/OrderPayService 파일 자체는 미수정 - 내장/기존 메서드만 사용)
    // ==========================================
    @GetMapping("/payment")
    public String payment(Model model,
                          @RequestParam(value = "page", defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "payment");
        model.addAttribute("pageTitle", "결제/수강 관리");

        Page<OrderPay> orders = orderPayRepository.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "payday"))
        );
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrderCount", orderPayRepository.count());

        List<OrderPay> allOrders = orderPayService.listAll();

        long successCount = allOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.SUCCESS).count();
        long cancelCount = allOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.CANCEL).count();
        long failedCount = allOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.FAILED).count();

        long totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderPayStatus.SUCCESS)
                .mapToLong(this::parsePrice)
                .sum();

        model.addAttribute("successCount", successCount);
        model.addAttribute("cancelCount", cancelCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("totalRevenue", totalRevenue);

        // ── 페이지네이션 번호 그룹 계산 (템플릿의 startPage/endPage/currentPage 에서 사용) ──
        int pageGroupSize = 10;
        int currentPage = orders.getNumber() + 1; // Page는 0부터 시작하므로 화면 표시용으로 1을 더함
        int totalPages = Math.max(orders.getTotalPages(), 1);
        int startPage = ((currentPage - 1) / pageGroupSize) * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "admin/payment";
    }

    // ==========================================
    // 사이트 설정 (백엔드 연동 전)
    // ==========================================
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "사이트 설정");
        model.addAttribute("settings", new SettingsMock());
        return "admin/settings";
    }

    // TODO: 실제 설정 저장 로직(DB/설정파일 연동)은 아직 없음 - 지금은 폼 제출 시 그대로 되돌아가기만 함
    @PostMapping("/settings")
    public String settingsSubmit() {
        return "redirect:/admin/settings";
    }

    // ==========================================
// 카테고리(강좌) 관리
// ==========================================
    @GetMapping("/category")
    public String category(Model model) {
        model.addAttribute("activeMenu", "category");
        model.addAttribute("pageTitle", "카테고리 관리");
        model.addAttribute("categories", categoryService.findAll());
        return "admin/admin_category/category";
    }

    @GetMapping("/category/create")
    public String categoryCreateForm(Model model) {
        model.addAttribute("activeMenu", "category");
        model.addAttribute("pageTitle", "카테고리 등록");
        model.addAttribute("categoryDTO", new CategoryDTO());
        return "admin/admin_category/category-create";
    }

    @PostMapping("/category/create")
    public String categoryCreateSubmit(@ModelAttribute CategoryDTO categoryDTO) {
        categoryService.chugaProc(categoryDTO);
        return "redirect:/admin/category";
    }

    @GetMapping("/category/edit/{id}")
    public String categoryEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "category");
        model.addAttribute("pageTitle", "카테고리 수정");

        Category category = categoryService.view(id);
        if (category == null) {
            return "redirect:/admin/category";
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
        dto.setInstructor(category.getInstructor());
        dto.setDescription(category.getDescription());
        dto.setFileName(category.getFileName()); // sujungProc이 이 값을 그대로 덮어쓰므로 반드시 유지해서 넘겨야 함

        model.addAttribute("categoryDTO", dto);
        model.addAttribute("existingCategory", category);
        return "admin/admin_category/category-edit";
    }

    @PostMapping("/category/update/{id}")
    public String categoryUpdateSubmit(@PathVariable Long id, @ModelAttribute CategoryDTO categoryDTO) {
        categoryDTO.setId(id);
        categoryService.sujungProc(categoryDTO);
        return "redirect:/admin/category";
    }

    @GetMapping("/category/delete/{id}")
    public String categoryDelete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin/category";
    }

    // ── 가짜/DTO 내부 클래스 구조들 ──
    public static class QnaWrapper {
        private final Long id; private final String title; private final String content; private final AuthorMock author; private final String createdDate; private final String status;
        public QnaWrapper(Long id, String title, String content, String username, String createdDate, String status) { this.id = id; this.title = title; this.content = content; this.author = new AuthorMock(username); this.createdDate = createdDate; this.status = status; }
        public Long getId() { return id; } public String getTitle() { return title; } public String getContent() { return content; } public AuthorMock getAuthor() { return author; } public String getCreatedDate() { return createdDate; } public String getStatus() { return status; }
    }
    public static class AuthorMock { private final String username; public AuthorMock(String username) { this.username = username; } public String getUsername() { return username; } }
    public static class EventMock {
        private final Long id; private final String title; private final String startDate; private final String endDate; private final int participantCount; private final String createdDate; private final String status;
        public EventMock(Long id, String title, String startDate, String endDate, int participantCount, String createdDate, String status) { this.id = id; this.title = title; this.startDate = startDate; this.endDate = endDate; this.participantCount = participantCount; this.createdDate = createdDate; this.status = status; }
        public Long getId() { return id; } public String getTitle() { return title; } public String getStartDate() { return startDate; } public String getEndDate() { return endDate; } public int getParticipantCount() { return participantCount; } public String getCreatedDate() { return createdDate; } public String getStatus() { return status; }
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
    // 사이트 설정 화면(admin/settings.html)용 임시 mock - 아직 실제 저장/조회 백엔드 연동 전
    public static class SettingsMock {
        private String siteName = "끼역띠귿";
        private String siteDescription = "외국인을 위한 한국어 학습 플랫폼";
        private boolean signupEnabled = true;
        private boolean emailVerification = false;
        private boolean rejoinEnabled = true;
        private int boardPageSize = 10;
        private boolean commentEnabled = true;
        private boolean attachmentEnabled = true;

        public String getSiteName() { return siteName; }
        public String getSiteDescription() { return siteDescription; }
        public boolean isSignupEnabled() { return signupEnabled; }
        public boolean isEmailVerification() { return emailVerification; }
        public boolean isRejoinEnabled() { return rejoinEnabled; }
        public int getBoardPageSize() { return boardPageSize; }
        public boolean isCommentEnabled() { return commentEnabled; }
        public boolean isAttachmentEnabled() { return attachmentEnabled; }
    }
}