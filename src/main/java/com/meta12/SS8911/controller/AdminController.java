package com.meta12.SS8911.controller;

import com.meta12.SS8911.config.OrderPayStatus;
import com.meta12.SS8911.dto.CategoryDTO;
import com.meta12.SS8911.dto.ContentDTO;
import com.meta12.SS8911.dto.EventCreateRequestDto;
import com.meta12.SS8911.dto.SettingsDTO;
import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.Event;
import com.meta12.SS8911.entity.OrderPay;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.OrderPayRepository;
import com.meta12.SS8911.service.CategoryService;
import com.meta12.SS8911.service.ContentService;
import com.meta12.SS8911.service.EventService;
import com.meta12.SS8911.service.OrderPayService;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SettingsService;
import com.meta12.SS8911.service.SiteUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private final EventService eventService;
    private final SettingsService settingsService;

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
        // ★ 구독 카테고리 접근권 레코드("구독 강의 접근")와 강사 칭찬 도장 레코드("강사 칭찬 도장")는
        //   진짜 결제 건이 아니라서 통계/최근 결제 내역에서 제외.
        List<OrderPay> allOrders = orderPayService.listAll().stream()
                .filter(o -> o.getPayType() == null ||
                        (!o.getPayType().trim().equals("구독 강의 접근")
                                && !o.getPayType().trim().equals("강사 칭찬 도장")))
                .collect(Collectors.toList());
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

        // ── 도넛 차트: 플랜별 결제 비율 (성공 결제 기준) ──
        List<OrderPay> successOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderPayStatus.SUCCESS)
                .collect(Collectors.toList());

        Map<String, Long> planCounts = successOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> planLabel(o.getPlanType()),
                        Collectors.counting()));

        List<CategoryStat> categoryStats = new ArrayList<>();
        String donutGradient = null;
        long totalSuccessCount = successOrders.size();

        if (totalSuccessCount > 0) {
            String[] donutColors = {"#185FA5", "#5DCAA5", "#9FE1CB", "#EF9F27", "#D85A30", "#E6F1FB"};
            List<Map.Entry<String, Long>> sorted = planCounts.entrySet().stream()
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

    // planType 코드를 도넛 차트 범례에 표시할 이름으로 변환
    private String planLabel(String planType) {
        if (planType == null) return "개별 구매";
        return switch (planType.trim()) {
            case "월구독" -> "월 구독";
            case "연구독" -> "연 구독";
            case "평생" -> "평생 소장";
            default -> planType;
        };
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

        // ★ 페이지네이션 대신 테마(카테고리)별로 묶어서 전체 보여주기 (36개 = 4테마 × 9강 정도라 아코디언이 더 적합)
        // Page.getContent()는 불변(읽기 전용) 리스트라 바로 sort()하면 UnsupportedOperationException이 남 → 새 ArrayList로 복사
        List<Content> allContents = new ArrayList<>(contentService.getAllContentList(
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.ASC, "sequence"))
        ).getContent());

        // 카테고리 id 순서(K-POP → K-DRAMA → 일상회화 → 심화)로 정렬 (Sort는 stable이라 테마 내 순서(sequence)는 유지됨)
        allContents.sort(java.util.Comparator.comparing(
                c -> c.getCategory() != null ? c.getCategory().getId() : Long.MAX_VALUE));

        java.util.LinkedHashMap<String, List<Content>> grouped = new java.util.LinkedHashMap<>();
        for (Content c : allContents) {
            String categoryName = c.getCategory() != null ? c.getCategory().getTitle() : "미지정";
            grouped.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(c);
        }

        model.addAttribute("groupedContents", grouped);
        model.addAttribute("totalContentCount", allContents.size());

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
//        contentService.delete(id);
        return "redirect:/admin/content";
    }

    // ==========================================
    // 회원 관리
    // ==========================================
    @GetMapping("/members")
    public String members(Model model,
                          @RequestParam(value = "page", defaultValue = "0") int page) {
        model.addAttribute("activeMenu", "members");
        model.addAttribute("pageTitle", "회원 관리");

        // ★ 회원 관리 목록에는 관리자(ADMIN) 계정은 보이지 않도록 제외
        Page<SiteUser> users = siteUserService.getNonAdminUsers(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "joinDate"))
        );
        model.addAttribute("users", users);

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

        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("category", category);
        extraParams.put("kw", kw);
        model.addAttribute("extraParams", extraParams);

        return "admin/board";
    }

    // ==========================================
    // 이벤트 관리 (Event 실데이터 - 포스터/썸네일 파일 업로드 포함)
    // ==========================================
    @GetMapping("/event")
    public String event(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page) {
        model.addAttribute("activeMenu", "event");
        model.addAttribute("pageTitle", "이벤트 관리");

        Page<Event> events = eventService.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
        );
        model.addAttribute("events", events);

        List<Event> allEvents = eventService.findAll();
        long ongoingCount = allEvents.stream().filter(e -> "ONGOING".equals(e.getStatus())).count();
        long upcomingCount = allEvents.stream().filter(e -> "UPCOMING".equals(e.getStatus())).count();
        long endedCount = allEvents.stream().filter(e -> "ENDED".equals(e.getStatus())).count();
        long totalParticipants = allEvents.stream()
                .mapToLong(e -> e.getParticipantCount() != null ? e.getParticipantCount() : 0)
                .sum();

        model.addAttribute("ongoingEventCount", ongoingCount);
        model.addAttribute("upcomingEventCount", upcomingCount);
        model.addAttribute("endedEventCount", endedCount);
        model.addAttribute("totalEventParticipants", totalParticipants);
        model.addAttribute("totalEventCount", events.getTotalElements());

        return "admin/event";
    }

    @GetMapping("/event/create")
    public String eventCreateForm(Model model) {
        model.addAttribute("activeMenu", "event");
        model.addAttribute("pageTitle", "이벤트 등록");
        model.addAttribute("eventCreateRequestDto", new EventCreateRequestDto());
        return "admin/event-create";
    }

    // 폼 enctype="multipart/form-data" 필수, input name="posterFile" / name="thumbnailFile"
    @PostMapping("/event/create")
    public String eventCreateSubmit(
            @ModelAttribute EventCreateRequestDto eventCreateRequestDto,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
    ) {
        eventService.create(eventCreateRequestDto, posterFile, thumbnailFile);
        return "redirect:/admin/event";
    }

    @GetMapping("/event/edit/{id}")
    public String eventEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "event");
        model.addAttribute("pageTitle", "이벤트 수정");

        Event event;
        try {
            event = eventService.findById(id);
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/event";
        }

        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle(event.getTitle());
        dto.setContent(event.getContent());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setStatus(event.getStatus());
        dto.setPoster(event.getPoster());
        dto.setThumbnail(event.getThumbnail());

        model.addAttribute("eventCreateRequestDto", dto);
        model.addAttribute("existingEvent", event);
        model.addAttribute("event", event);   // ⭐ 추가

        return "admin/eventEdit";
    }

    // 폼 enctype="multipart/form-data" 필수. 파일을 새로 선택 안 하면 기존 포스터/썸네일 유지됨
    @PostMapping("/event/update/{id}")
    public String eventUpdateSubmit(
            @PathVariable Long id,
            @ModelAttribute EventCreateRequestDto eventCreateRequestDto,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
    ) {
        eventService.update(id, eventCreateRequestDto, posterFile, thumbnailFile);
        return "redirect:/admin/event";
    }

    @GetMapping("/event/delete/{id}")
    public String eventDelete(@PathVariable Long id) {
        eventService.delete(id);
        return "redirect:/admin/event";
    }

    // ==========================================
    // 결제/수강 관리 — OrderPay 실데이터 연동
    // (OrderPayRepository/OrderPayService 파일 자체는 미수정 - 내장/기존 메서드만 사용)
    // ==========================================
    @GetMapping("/payment")
    public String payment(Model model,
                          @RequestParam(value = "page", defaultValue = "0") int page) {

        model.addAttribute("activeMenu", "payment");
        model.addAttribute("pageTitle", "결제/수강 관리");

        // ★ 구독 카테고리 접근권 레코드("구독 강의 접근")와 강사 칭찬 도장 레코드("강사 칭찬 도장")는
        //   진짜 결제 건이 아니라서 관리자 화면에서도 제외하고 대표 결제 건만 보여줌.
        Page<OrderPay> orders = orderPayRepository.findRealPayments(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "payday"))
        );
        model.addAttribute("orders", orders);

        List<OrderPay> allOrders = orderPayService.listAll();
        List<OrderPay> realOrders = allOrders.stream()
                .filter(o -> o.getPayType() == null ||
                        (!o.getPayType().trim().equals("구독 강의 접근")
                                && !o.getPayType().trim().equals("강사 칭찬 도장")))
                .collect(Collectors.toList());

        model.addAttribute("totalOrderCount", realOrders.size());

        long successCount = realOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.SUCCESS).count();
        long cancelCount = realOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.CANCEL).count();
        long failedCount = realOrders.stream().filter(o -> o.getStatus() == OrderPayStatus.FAILED).count();

        long totalRevenue = realOrders.stream()
                .filter(o -> o.getStatus() == OrderPayStatus.SUCCESS)
                .mapToLong(this::parsePrice)
                .sum();

        model.addAttribute("successCount", successCount);
        model.addAttribute("cancelCount", cancelCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin/payment";
    }

    // ==========================================
    // 사이트 설정 (DB 연동)
    // ==========================================
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "사이트 설정");
        model.addAttribute("settings", settingsService.get());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String settingsSubmit(@ModelAttribute SettingsDTO settingsDTO) {
        settingsService.save(settingsDTO);
        return "redirect:/admin/settings";
    }

    // ==========================================
    // 카테고리(강좌) 관리
    // ==========================================
    @GetMapping("/category")
    public String category(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page) {
        model.addAttribute("activeMenu", "category");
        model.addAttribute("pageTitle", "카테고리 관리");

        // ★ 번호(#)는 "등록한 순서" 그대로 고정 (K-POP이 제일 먼저 만든 거면 항상 1번),
        //   화면에 보이는 목록 순서는 최신 등록순(DESC) — 이 둘은 서로 다른 기준이라 분리해서 처리
        List<Category> allAscById = new ArrayList<>(categoryService.findAll());
        allAscById.sort(java.util.Comparator.comparing(Category::getId));

        java.util.Map<Long, Integer> registrationNumberByCategoryId = new HashMap<>();
        for (int i = 0; i < allAscById.size(); i++) {
            registrationNumberByCategoryId.put(allAscById.get(i).getId(), i + 1);
        }

        Page<Category> categories = categoryService.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("categories", categories);
        model.addAttribute("registrationNumberByCategoryId", registrationNumberByCategoryId);

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