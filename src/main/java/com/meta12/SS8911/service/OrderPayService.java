package com.meta12.SS8911.service;


//import com.example.masil.dto.OrderPayDTO;
//import com.example.masil.entity.Category;
//import com.example.masil.entity.OrderPay;
//
//import com.example.masil.entity.SiteUser;
//
//import com.example.masil.repository.CategoryRepository;
//import com.example.masil.repository.OrderPayRepository;
//
//import com.example.masil.repository.SiteUserRepository;
import com.meta12.SS8911.dto.MyPaymentDTO;
import com.meta12.SS8911.dto.OrderPayDTO;
import com.meta12.SS8911.entity.Category;

import com.meta12.SS8911.config.OrderPayStatus;
import com.meta12.SS8911.entity.OrderPay;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.CategoryRepository;
import com.meta12.SS8911.repository.OrderPayRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderPayService {
    private final OrderPayRepository orderPayRepository;
    private final SiteUserRepository siteUserRepository;
    private final CategoryRepository categoryRepository;

    // 구독 결제 시, 카테고리 접근권한용 레코드의 payType에 찍어두는 내부 마커.
    // 구매 내역(마이페이지)에서는 이 마커가 찍힌 행을 제외하고,
    // 아래에서 생성하는 "대표 결제 레코드" 1건만 노출합니다.
    private static final String SUBSCRIPTION_ACCESS_MARKER = "구독 강의 접근";

    // 기존 호출부(레거시 /order/subscribe 등) 하위호환용 - orderId/paymentKey 없이 호출
    @Transactional
    public void subscribeAllCategories(String username, String planName, String price, String payType, String cardNumber) {
        subscribeAllCategories(username, planName, price, payType, cardNumber, null, null);
    }

    // 구독 결제: 플랜을 사면 (1) "실제 결제 1건"을 나타내는 대표 레코드를 항상 새로 만들고,
    // (2) 모든 카테고리에 대해 접근 권한용 레코드를 만들어줍니다. (이미 접근권한이 있는 카테고리는 건너뜀)
    // ★ orderId/paymentKey: 토스 결제 연동 시 결제 고유 식별자 - 엔티티 전용 필드에 저장
    //   (예전엔 paymentKey를 cardNumber 필드에 잘못 저장하고 있었음 → 전용 필드로 수정)
    @Transactional
    public void subscribeAllCategories(String username, String planName, String price, String payType,
                                       String cardNumber, String orderId, String paymentKey) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 1) 마이페이지 "구매 내역"에 뜨는 대표 결제 레코드 (카테고리 없음, 플랜명 표시용)
        //    -> 재결제(갱신)할 때마다 매번 새로 쌓여서 결제 이력이 누락되지 않습니다.
        OrderPay mainOrder = new OrderPay();
        mainOrder.setSiteUser(user);
        mainOrder.setCategory(null);
        mainOrder.setPrice(price);
        mainOrder.setPayType(payType);
        mainOrder.setCardNumber(cardNumber);
        mainOrder.setPlanType(planName); // ★ 플랜명은 planType에 저장 (instructorName은 실제 강사용 필드라 건드리지 않음)
        mainOrder.setOrderId(orderId);       // ★ 토스 주문번호 (토스 연동 아니면 null)
        mainOrder.setPaymentKey(paymentKey); // ★ 토스 결제 고유 키 (토스 연동 아니면 null)
        mainOrder.setPayday(LocalDateTime.now());
        mainOrder.setStatus(OrderPayStatus.SUCCESS); // ★ 실제 결제가 승인된 시점이므로 SUCCESS로 표시 (안 하면 매출 집계에서 누락됨)
        orderPayRepository.save(mainOrder);

        // 2) 카테고리별 접근 권한 부여용 레코드 (구매 내역 화면에서는 숨겨짐)
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            if (orderPayRepository.existsBySiteUserAndCategory(user, category)) {
                continue; // 이미 접근권한이 있는 카테고리는 건너뜀
            }

            OrderPay orderPay = new OrderPay();
            orderPay.setSiteUser(user);
            orderPay.setCategory(category);
            orderPay.setPrice(price);
            orderPay.setPayType(SUBSCRIPTION_ACCESS_MARKER); // 실제 결제수단 대신 마커를 저장
            orderPay.setCardNumber(cardNumber);
            orderPay.setPlanType(planName); // ★ instructorName 대신 planType에 저장
            orderPay.setPayday(LocalDateTime.now());
            orderPay.setStatus(OrderPayStatus.SUCCESS); // 같은 결제 건이므로 상태 일관성 유지 (통계에서는 마커로 별도 제외됨)

            orderPayRepository.save(orderPay);
        }
    }

    // ============================================================
    // ★ 토스 결제 승인 실패 시 이력을 남기는 메서드.
    // subscribeAllCategories()와 달리 카테고리 접근 권한은 부여하지 않고
    // "실패했다"는 사실만 대표 레코드 1건으로 남김 (CS 대응/통계용).
    // ★ status=FAILED로 저장되므로 findRealPayments()의 status 필터에 의해
    //   관리자 매출 통계에서는 자동으로 제외됨.
    // ============================================================
    @Transactional
    public void saveFailedPayment(String username, String planName, int amount, String payType,
                                  String orderId, String failReason) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        OrderPay failedOrder = new OrderPay();
        failedOrder.setSiteUser(user);
        failedOrder.setCategory(null);
        failedOrder.setPrice(String.valueOf(amount));
        failedOrder.setPayType(payType);
        failedOrder.setOrderId(orderId); // ★ 토스 주문번호 - 전용 필드에 저장 (실패 사유 자체는 서버 로그로 확인)
        failedOrder.setPlanType(planName);
        failedOrder.setPayday(LocalDateTime.now());
        failedOrder.setStatus(OrderPayStatus.FAILED);

        orderPayRepository.save(failedOrder);
    }

    public List<OrderPay> list(String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return orderPayRepository.findBySiteUser(user);
    }

    public List<OrderPay> listAll() {
        return orderPayRepository.findAll();
    }

    public OrderPay view(long id) {
        OrderPay orderPay = null;
        Optional<OrderPay> op = orderPayRepository.findById(id);
        if (op.isPresent()) {
            orderPay = op.get();
        }
        return orderPay;
    }

    //    public void chugaProc(OrderPayDTO orderPayDTO, Long siteUserId) {
//        SiteUser siteUser = siteUserRepository.findById(siteUserId)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//        // 엔티티 만들 때 유저를 직접 셋팅
//        OrderPay orderPay = createEntity(orderPayDTO, siteUser);
//        orderPayRepository.save(orderPay);
//    }
    public void chugaProc(OrderPayDTO orderPayDTO, String categoryTitle, Long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Category category = categoryRepository.findByTitle(categoryTitle)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + categoryTitle));

        OrderPay orderPay = new OrderPay();
        orderPay.setCategory(category);
        orderPay.setPrice(orderPayDTO.getPrice());
        orderPay.setSiteUser(siteUser);
        orderPay.setInstructorName(orderPayDTO.getInstructorName());

        orderPayRepository.save(orderPay); // ← 이게 빠져있었어요!
    }

    @Transactional
    public void sujungProc(OrderPayDTO dto, Long userId) {
        // [로그 1] 서비스 진입 확인
        System.out.println("====== 서비스 sujungProc 시작 ======");

        // [로그 2] 전달받은 DTO 자체의 상태 확인
        if (dto == null) {
            System.out.println("에러: 전달된 DTO 자체가 null입니다.");
            throw new RuntimeException("DTO가 null입니다.");
        }

        // [로그 3] ID값 확인 (여기가 null이면 컨트롤러에서 배달 사고)
        System.out.println("서비스가 받은 ID: " + dto.getId());
        System.out.println("서비스가 받은 강사명: " + dto.getInstructorName());

        if (dto.getId() == null) {
            throw new RuntimeException("수정 실패: 서비스로 넘어온 ID가 null입니다. DTO 설정을 확인하세요.");
        }

        // [중요] ID를 별도 변수에 담아서 findById 실행
        Long targetId = dto.getId();

        OrderPay orderPay = orderPayRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("해당 강좌를 찾을 수 없습니다. ID: " + targetId));

        // 데이터 덮어쓰기
        orderPay.setCategory(dto.getCategory());
        orderPay.setInstructorName(dto.getInstructorName());
        orderPay.setPrice(dto.getPrice()); // 엔티티와 DTO 모두 String이므로 문제 없음

        System.out.println("====== 서비스 sujungProc 완료 ======");
    }

    @Transactional
    public void sakjeProc(Long id) {
        // 해당 ID가 있는지 확인 후 삭제
        if (orderPayRepository.existsById(id)) {
            orderPayRepository.deleteById(id);
        } else {
            throw new RuntimeException("삭제할 대상이 없습니다.");
        }
    }


    // OrderPayService.java 내부의 createEntity 메서드 수정
    private OrderPay createEXntity(OrderPayDTO orderPayDTO, SiteUser siteUser) {
        OrderPay orderPay = new OrderPay();
        orderPay.setId(orderPayDTO.getId());
        orderPay.setCategory(orderPayDTO.getCategory());
        orderPay.setPrice(orderPayDTO.getPrice());

        // [수정] 강좌 등록 시에는 날짜를 채우지 않습니다. (null 상태 유지)
        // orderPay.setPayday(LocalDateTime.now()); <--- 이 줄을 삭제하거나 주석 처리하세요.

        orderPay.setSiteUser(siteUser); // 등록한 관리자 정보
        orderPay.setInstructorName(orderPayDTO.getInstructorName());
        orderPay.setPayType(orderPayDTO.getPayType());
        orderPay.setCardNumber(orderPayDTO.getCardNumber());
        return orderPay;
    }

    @Transactional
    public void updatePaymentInfo(Long id, String cardNumber, String payType, String username) {
        // 1. 결제할 주문 내역 조회
        OrderPay orderPay = orderPayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("주문 내역을 찾을 수 없습니다."));

        // 2. 결제한 사용자(학생) 조회
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 3. 결제 정보 및 사용자 연결 (가장 중요!)
        orderPay.setCardNumber(cardNumber);
        orderPay.setPayType(payType);
        orderPay.setPayday(LocalDateTime.now());
        orderPay.setSiteUser(user); // 여기서 사용자를 연결해줘야 '나의 공부방'에 뜹니다!
    }
    // 결제 완료 시 날짜를 채워주는 로직 추가 (updatePaymentInfo 수정)
//    @Transactional
//    public void updatePaymentInfo(Long id, String cardNumber) {
//        OrderPay orderPay = orderPayRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("주문 내역을 찾을 수 없습니다."));
//
//        orderPay.setCardNumber(cardNumber);
//        // [추가] 실제 결제가 완료되는 이 시점에 날짜를 저장합니다.
//        orderPay.setPayday(LocalDateTime.now());
//    }
//    // OrderPayService.java 파일에 추가
//    @org.springframework.transaction.annotation.Transactional
//    public void updatePaymentInfo(Long id, String cardNumber) {
//        OrderPay orderPay = orderPayRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("주문 내역을 찾을 수 없습니다."));
//
//        // 카드 번호 저장 (보안상 뒷자리만 저장하거나 가짜로 저장)
//        // OrderPay 엔티티에 cardNumber 필드가 있어야 합니다.
//        orderPay.setCardNumber(cardNumber);
//
//        // 수강료 지불 상태 등을 변경하고 싶다면 여기서 세팅
//        // orderPay.setStatus("결제완료");
//
//        // @Transactional이 붙어있으면 자동으로 save됩니다.
//    }

    // 사용자가 신청한(결제일이 있는) 강좌의 ID 리스트만 가져오는 메서드
    public List<Long> getMyAppliedCourseIds(String username) {
        // 1. 해당 사용자의 전체 결제 내역을 가져옵니다.
        List<OrderPay> myOrders = orderPayRepository.findBySiteUser_Username(username);

        // 2. 그중에서 결제일(payday)이 실제로 존재하는 것들의 'ID'만 추출합니다.
        return myOrders.stream()
                .filter(order -> order.getPayday() != null) // 결제일이 있는 것만 필터링
                .map(OrderPay::getId)                       // ID 값으로 변환
                .collect(Collectors.toList());              // 리스트로 수집
    }

    public List<SiteUser> getUniqueStudents(Long categoryId) {
        // 1. 특정 카테고리의 모든 주문 내역을 가져옵니다.
        List<OrderPay> orders = orderPayRepository.findByCategoryId(categoryId);

        // 2. 주문 내역에서 사용자(SiteUser)만 추출한 뒤, distinct()로 중복을 제거합니다.
        return orders.stream()
                .map(OrderPay::getSiteUser) // OrderPay에서 SiteUser 추출
                .filter(user -> user != null) // 혹시 모를 null 방지
                .distinct()                // 🌟 핵심: 동일한 사용자 객체 중복 제거
                .collect(Collectors.toList());
    }

    public List<MyPaymentDTO> getMyPayments(String username) {

        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow();

        List<OrderPay> list = orderPayRepository.findBySiteUser(user);

        return list.stream()
                .filter(o ->
                        o.getPayType() == null ||
                                (!o.getPayType().equals("강사 칭찬 도장")
                                        && !o.getPayType().equals("구독 강의 접근")))
                .map(this::convert)
                .toList();
    }

    private MyPaymentDTO convert(OrderPay order) {

        MyPaymentDTO dto = new MyPaymentDTO();

        dto.setPrice(order.getPrice());
        dto.setPayType(order.getPayType());
        dto.setPayday(order.getPayday());

        // 구매 상품명
        if (order.getPlanType() != null) {

            dto.setProductName(order.getPlanType());

        } else if (order.getCategory() != null) {

            dto.setProductName(order.getCategory().getTitle());

        }

        // 유효기간
        if (order.getPayday() != null) {

            if ("월구독".equals(order.getPlanType())) {

                dto.setPeriod(
                        order.getPayday().toLocalDate()
                                + " ~ "
                                + order.getPayday().plusMonths(1).toLocalDate()
                );

            } else if ("연구독".equals(order.getPlanType())) {

                dto.setPeriod(
                        order.getPayday().toLocalDate()
                                + " ~ "
                                + order.getPayday().plusYears(1).toLocalDate()
                );

            } else {

                dto.setPeriod("평생 이용");

            }

        }

        return dto;
    }
}