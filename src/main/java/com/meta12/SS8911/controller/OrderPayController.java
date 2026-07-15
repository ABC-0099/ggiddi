package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.OrderPayDTO;
import com.meta12.SS8911.dto.SubscriptionPlanDTO;
import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.OrderPay;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.SiteUserRepository;

import com.meta12.SS8911.service.CategoryService;
import com.meta12.SS8911.service.ContentService;
import com.meta12.SS8911.service.OrderPayService;
import com.meta12.SS8911.service.SiteUserService;
import com.meta12.SS8911.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderPayController {
    private final OrderPayService orderPayService;
    private final SiteUserRepository siteUserRepository;
    private final SiteUserService siteUserService;
    private final ContentService contentService;
    private final CategoryService categoryService;
    private final TossPaymentService tossPaymentService; // ★ 토스 결제 승인용 서비스 추가

    @Value("${toss.payments.client-key}")
    private String tossClientKey; // ★ 프론트에 넘길 클라이언트 키 (secret key는 절대 여기 안 씀)


    @GetMapping("/orderPay/list")
    public String list(Model model, Principal principal) {
        List<OrderPay> list = orderPayService.listAll();
        model.addAttribute("list", list);

        // 기본값은 빈 리스트로 설정 (로그인 안 한 사람 대비)
        List<Long> myAppliedIds = Collections.emptyList();

        if (principal != null) {
            // 로그인 상태라면 실제 신청한 ID 목록을 가져옴
            myAppliedIds = orderPayService.getMyAppliedCourseIds(principal.getName());
        }

        model.addAttribute("myAppliedIds", myAppliedIds);

        // 구독 결제 화면(플랜 선택)에 필요한 데이터
        List<SubscriptionPlanDTO> plans = buildDefaultPlans();
        model.addAttribute("plans", plans);
        model.addAttribute("defaultPlan", plans.stream()
                .filter(SubscriptionPlanDTO::isPopular)
                .findFirst()
                .orElse(plans.get(0)));

        model.addAttribute("tossClientKey", tossClientKey); // ★ 결제창 호출용 클라이언트 키 전달
        // ★ 결제위젯은 구매자를 식별하는 customerKey가 필수. 비로그인이면 위젯 쪽 익명 처리 규칙(ANONYMOUS)에 맞춤.
        //   주의: username은 유추 가능한 값이라 실제 운영 단계에서는 UUID 기반으로 바꾸는 걸 권장.
        model.addAttribute("customerKey", principal != null ? principal.getName() : "ANONYMOUS");

        return "orderPay/list";
    }

    /**
     * 월구독 / 연구독 / 평생소장 3개 플랜의 고정 데이터를 만듭니다.
     * 추후 DB(Category 등)로 옮기고 싶다면 이 메서드만 교체하면 됩니다.
     */
    private List<SubscriptionPlanDTO> buildDefaultPlans() {
        List<SubscriptionPlanDTO> plans = new java.util.ArrayList<>();

        plans.add(new SubscriptionPlanDTO(
                "월구독", "월 구독", 9900, 9900, 0,
                "/ 매월 자동 갱신", null, false, "구독 시작하기",
                List.of("첫 구독 시 7일 무료 체험", "언제든지 해지 가능", "다음 결제일 전 알림 발송"),
                List.of(
                        new SubscriptionPlanDTO.Feature("전체 강의 이용", true),
                        new SubscriptionPlanDTO.Feature("RPG 게임 학습", true),
                        new SubscriptionPlanDTO.Feature("단계별 퀴즈", true),
                        new SubscriptionPlanDTO.Feature("커뮤니티 이용", false),
                        new SubscriptionPlanDTO.Feature("학습 분석 리포트", false),
                        new SubscriptionPlanDTO.Feature("평생 업데이트", false)
                )
        ));

        plans.add(new SubscriptionPlanDTO(
                "연구독", "연 구독", 89900, 118800, 28900,
                "/ 매년 자동 갱신", "월 7,492원 · 25% 절약", true, "구독 시작하기",
                List.of("첫 구독 시 7일 무료 체험", "언제든지 해지 가능", "다음 결제일 전 알림 발송"),
                List.of(
                        new SubscriptionPlanDTO.Feature("전체 강의 이용", true),
                        new SubscriptionPlanDTO.Feature("RPG 게임 학습", true),
                        new SubscriptionPlanDTO.Feature("단계별 퀴즈", true),
                        new SubscriptionPlanDTO.Feature("커뮤니티 이용", true),
                        new SubscriptionPlanDTO.Feature("학습 분석 리포트", true),
                        new SubscriptionPlanDTO.Feature("평생 업데이트", false)
                )
        ));

        plans.add(new SubscriptionPlanDTO(
                "평생", "평생 소장 ✨", 199000, 199000, 0,
                "/ 1회 결제 · 영구 이용", "가장 합리적인 선택", false, "결제하기",
                List.of("한 번만 결제, 영구 이용", "신규 강의 무료 업데이트", "커뮤니티 평생 이용 가능"),
                List.of(
                        new SubscriptionPlanDTO.Feature("전체 강의 이용", true),
                        new SubscriptionPlanDTO.Feature("RPG 게임 학습", true),
                        new SubscriptionPlanDTO.Feature("단계별 퀴즈", true),
                        new SubscriptionPlanDTO.Feature("커뮤니티 이용", true),
                        new SubscriptionPlanDTO.Feature("학습 분석 리포트", true),
                        new SubscriptionPlanDTO.Feature("평생 업데이트 포함", true)
                )
        ));

        return plans;
    }

    @GetMapping("/orderPay/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id
    ) {
        OrderPay orderPay = orderPayService.view(id);
        model.addAttribute("orderPay", orderPay);

        return "orderPay/view";
    }

    @GetMapping("/orderPay/chuga")
    public String chuga(Model model, Principal principal) {

        if (principal != null) {
            System.out.println("현재 로그인한 아이디: " + principal.getName());
        }

        if (principal == null || !"admin888".equalsIgnoreCase(principal.getName())) {
            return "redirect:/orderPay/list";
        }

        List<Category> categoryList = categoryService.findAll(); // 추가
        model.addAttribute("categoryList", categoryList);        // 추가

        return "orderPay/chuga";
    }
    @GetMapping("/orderPay/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id,
            Principal principal
    ) {
        // 모든 체크 로직을 아래와 같이 변경해 보세요.
        if (principal == null || !"admin888".equalsIgnoreCase(principal.getName())) {
            return "redirect:/orderPay/list";
        }

        OrderPay orderPay = orderPayService.view(id);
        model.addAttribute("orderPay", orderPay);
        return "orderPay/sujung";
    }

    @GetMapping("/orderPay/sakje/{id}")
    @ResponseBody // <-- 자바스크립트 통신을 위해 추가
    public ResponseEntity<String> sakje(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        // 1. 관리자 체크
        if (principal == null || !"admin888".equalsIgnoreCase(principal.getName())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        try {
            // 2. 서비스에서 삭제 실행
            orderPayService.sakjeProc(id);

            // 3. 성공 응답 전송
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }


    @PostMapping("/orderPay/chugaProc")
    public String chugaProc(OrderPayDTO orderPayDTO, Principal principal) {
        // 모든 체크 로직을 아래와 같이 변경해 보세요.
        if (principal == null || !"admin888".equalsIgnoreCase(principal.getName())) {
            return "redirect:/orderPay/list";
        }
        SiteUser user = siteUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String categoryTitle = orderPayDTO.getCategory().getTitle();

// 이제 정상적으로 3개의 인자가 전달됩니다.
        orderPayService.chugaProc(orderPayDTO, categoryTitle, user.getId());

        return "redirect:/orderPay/list";
    }

    @PostMapping("/orderPay/sujungProc")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> sujungProc(OrderPayDTO orderPayDTO, Principal principal) {

        // 1. 관리자 권한 체크
        if (principal == null || !"admin888".equalsIgnoreCase(principal.getName())) {
            return org.springframework.http.ResponseEntity.status(403).body("권한이 없습니다.");
        }

        // [수정] 로그는 return이 일어나기 전에 찍어야 합니다!
        System.out.println("넘어온 ID 값: " + orderPayDTO.getId());
        System.out.println("넘어온 강사명: " + orderPayDTO.getInstructorName());

        try {
            // 2. 수정 로직 실행
            orderPayService.sujungProc(orderPayDTO, orderPayDTO.getSiteUserId());

            // 3. 성공 시 여기서 메서드가 완전히 종료됩니다.
            return org.springframework.http.ResponseEntity.ok("Success");

        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(500).body("수정 중 오류 발생: " + e.getMessage());
        }

        // 이 아래에 있던 중복 코드들을 삭제했습니다. (여기가 unreachable 구역)
    }

//@PostMapping("/orderPay/sakjeProc")
//public String sakjeProc(OrderPayDTO orderPayDTO){
//    SiteUser siteUser = new SiteUser();
//    siteUser.setId(orderPayDTO.getSiteUserId());
//    orderPayService.sakjeProc(orderPayDTO, siteUser);
//    return  "redirect:/orderPay/list/";
//}

    @PostMapping("/orderPay/sakjeProc")
    public String sakjeProc(OrderPayDTO orderPayDTO, Principal principal) {
        if (principal == null || !"admin888".equalsIgnoreCase(principal.getName())) {
            return "redirect:/orderPay/list";
        }

        // 수정(sujungProc)이 아니라 삭제(sakjeProc)를 호출해야 합니다!
        orderPayService.sakjeProc(orderPayDTO.getId());
        return "redirect:/orderPay/list";
    }


    // 결제 페이지(플랜 선택)에서 "결제하기" 누르면 호출되는 엔드포인트.
    // 새 주문 생성 + 결제 완료 처리를 한번에 합니다.
    // ⚠️ 참고: 토스 연동 전 방식 (카드번호를 프론트에서 직접 받아 바로 저장).
    // 토스 연동(/payment/toss/success)이 정상 동작 확인되면 이 엔드포인트는 제거해도 됩니다.
    @PostMapping("/order/subscribe")
    @ResponseBody
    public ResponseEntity<String> subscribe(@RequestBody Map<String, Object> params, Principal principal) {
        if (principal == null) return ResponseEntity.status(403).body("로그인이 필요합니다.");

        try {
            String planName = String.valueOf(params.get("planName"));
            String price = String.valueOf(params.get("price"));
            String payType = String.valueOf(params.get("payType"));
            String cardNumber = params.get("cardNumber") != null ? String.valueOf(params.get("cardNumber")) : "";

            orderPayService.subscribeAllCategories(principal.getName(), planName, price, payType, cardNumber);

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("결제 처리 중 오류: " + e.getMessage());
        }
    }

    // ============================================================
    // ★ 토스페이먼츠 연동 - 결제 승인 콜백
    // 프론트에서 tossPayments.requestPayment(...) 호출 후,
    // 토스 결제창에서 결제가 끝나면 여기 successUrl로 리다이렉트됨.
    // paymentKey/orderId/amount는 토스가 자동으로 붙여서 보내주고,
    // planName/payType은 successUrl 만들 때 우리가 쿼리파라미터로 미리 붙여둔 값.
    // ============================================================
    @GetMapping("/payment/toss/success")
    public String tossSuccess(@RequestParam String paymentKey,
                              @RequestParam String orderId,
                              @RequestParam int amount,
                              @RequestParam String planName,
                              @RequestParam String payType,
                              Principal principal,
                              Model model) {
        if (principal == null) return "redirect:/siteUser/login";

        try {
            // 1. 토스 서버에 실제 결제 승인 요청 (여기서 금액 위변조 등은 토스가 걸러냄)
            tossPaymentService.confirmPayment(paymentKey, orderId, amount);

            // 2. 승인 성공한 경우에만 구독/결제 내역을 DB에 반영
            //    orderId/paymentKey는 엔티티 전용 필드에 저장 (cardNumber에는 넣지 않음 - 카드번호 입력 자체가 없음)
            orderPayService.subscribeAllCategories(
                    principal.getName(), planName, String.valueOf(amount), payType, null, orderId, paymentKey
            );

            model.addAttribute("planName", planName);
            model.addAttribute("amount", amount);
            return "payment/success";
        } catch (Exception e) {
            e.printStackTrace();
            // ★ 승인 실패 시 이력을 남겨 CS 대응/통계에 활용
            orderPayService.saveFailedPayment(principal.getName(), planName, amount, payType, orderId, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "payment/fail";
        }
    }

    // 토스 결제창에서 사용자가 결제를 취소하거나 실패했을 때 리다이렉트되는 곳
    @GetMapping("/payment/toss/fail")
    public String tossFail(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String message,
                           Model model) {
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);
        return "payment/fail";
    }


    // OrderPayController.java의 completeOrder 메서드 수정
    @PostMapping("/order/complete")
    @ResponseBody
    public ResponseEntity<String> completeOrder(@RequestBody Map<String, Object> params, Principal principal) {
        if (principal == null) return ResponseEntity.status(403).body("로그인이 필요합니다.");

        Long orderId = Long.parseLong(params.get("orderId").toString());
        String cardNumber = params.get("cardNumber").toString();
        String payType = params.get("payType").toString();

        // 서비스 호출 시 principal.getName()을 꼭 같이 보내주세요.
        orderPayService.updatePaymentInfo(orderId, cardNumber, payType, principal.getName());

        return ResponseEntity.ok("Success");
    }
    // OrderPayController.java 수정
    // OrderPayController.java

    // OrderPayController.java 내의 showMyRoom 메서드 수정

    // OrderPayController.java 수정

    @GetMapping("/information/list")
    public String showMyRoom(Model model, Principal principal) {
        if (principal == null) return "redirect:/siteUser/login";

        // 1. 서비스에서 사용자의 모든 내역(결제 + 도장 등)을 가져옵니다.
        List<OrderPay> allList = orderPayService.list(principal.getName());

        // 2. 필터링: "강사 칭찬 도장"인 항목은 제외하고 "진짜 결제 내역"만 추출합니다.
        List<OrderPay> realPaymentList = new java.util.ArrayList<>();
        for (OrderPay pay : allList) {
            // 도장 데이터의 payType이 정확히 "강사 칭찬 도장"인지 확인합니다.
            // 데이터베이스에 공백이 있을 수 있으니 trim()을 사용합니다.
            if (pay.getPayType() == null ||
                    (!pay.getPayType().trim().equals("강사 칭찬 도장")      // 도장 데이터 제외
                            && !pay.getPayType().trim().equals("구독 강의 접근"))) { // 구독 카테고리 접근권한용 레코드 제외
                realPaymentList.add(pay);
            }
        }

        // 3. 필터링 된 리스트를 모델에 담습니다.
        model.addAttribute("list", realPaymentList);

        // 4. 기존 출석 체크 로직 유지

        SiteUser siteUser = siteUserService.getUser(principal.getName());

        SiteUser user = siteUserService.getUser(principal.getName());
        double avg = contentService.getAverageProgress(user);
        model.addAttribute("avgProgress", Math.round(avg));

        model.addAttribute("courseProgressList", contentService.getCourseProgressList(user));


        return "information/list"; // 리턴할 HTML 경로가 맞는지 다시 확인하세요!
    }




}