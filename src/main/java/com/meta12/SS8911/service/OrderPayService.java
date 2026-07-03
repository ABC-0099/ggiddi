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
import com.meta12.SS8911.dto.OrderPayDTO;
import com.meta12.SS8911.entity.Category;

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

    // 구독 결제: 플랜을 사면 모든 카테고리에 대해 결제 완료(OrderPay) 처리를 해줍니다.
    // 이미 결제된 카테고리는 중복 생성하지 않습니다.
    @Transactional
    public void subscribeAllCategories(String username, String planName, String price, String payType, String cardNumber) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            if (orderPayRepository.existsBySiteUserAndCategory(user, category)) {
                continue; // 이미 결제된 카테고리는 건너뜀
            }

            OrderPay orderPay = new OrderPay();
            orderPay.setSiteUser(user);
            orderPay.setCategory(category);
            orderPay.setPrice(price);
            orderPay.setPayType(payType);
            orderPay.setCardNumber(cardNumber);
            orderPay.setInstructorName(planName); // 구독 플랜명 기록용
            orderPay.setPayday(LocalDateTime.now());

            orderPayRepository.save(orderPay);
        }
    }

    public List<OrderPay> list(String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return orderPayRepository.findBySiteUser(user);
    }

    public List<OrderPay> listAll() {
        return orderPayRepository.findAll();
    }

    public OrderPay view(long id){
        OrderPay orderPay = null;
        Optional<OrderPay> op = orderPayRepository.findById(id);
        if(op.isPresent()){
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
    private OrderPay createEXntity(OrderPayDTO orderPayDTO, SiteUser siteUser){
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
}