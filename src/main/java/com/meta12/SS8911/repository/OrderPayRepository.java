package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.OrderPay;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderPayRepository extends JpaRepository<OrderPay, Long> {

    List<OrderPay> findBySiteUser(SiteUser siteUser);

    List<OrderPay> findBySiteUser_Username(String username);

    // 🛠️ [서버 기동 에러 해결]: String 대신 Category 객체 타입을 받도록 수정합니다!
    List<OrderPay> findByCategory(Category category);
    // Category 객체 안의 'title' 필드 문자열과 매칭하여 리스트를 뽑아오는 내장 문법입니다!
    List<OrderPay> findByCategory_Title(String categoryTitle);

    // 1. 기존 메서드 (객체 기반)
    boolean existsBySiteUserAndCategory(SiteUser siteUser, Category category);

    // 2. 새 메서드 (String 기반 - 이걸 추가하세요!)
    boolean existsBySiteUserAndCategory_Title(SiteUser siteUser, String categoryTitle);

    List<OrderPay> findByCategoryId(Long categoryId);
    Optional<OrderPay> findBySiteUserAndCategory(SiteUser siteUser, Category category);

    void deleteByCategoryId(Long categoryId);

    // ★ 관리자 결제 내역 화면(/admin/payment)용 - 구독 카테고리 접근권 레코드(payType == "구독 강의 접근")와
    //   강사 칭찬 도장 레코드(payType == "강사 칭찬 도장")는 제외하고 "진짜 결제 건"만 페이징 조회.
    //   payType이 null인 정상 결제도 있으므로 반드시 포함되도록 조건 구성.
    //   ★ status도 SUCCESS(또는 레거시 null)만 통과시켜서, 토스 결제 실패(FAILED)/취소(CANCEL) 건이
    //   매출 통계·도넛차트에 섞여 들어가지 않도록 함.
    @Query("SELECT o FROM OrderPay o WHERE (o.payType IS NULL OR o.payType NOT IN ('구독 강의 접근', '강사 칭찬 도장')) " +
            "AND (o.status IS NULL OR o.status = com.meta12.SS8911.config.OrderPayStatus.SUCCESS)")
    Page<OrderPay> findRealPayments(Pageable pageable);
}