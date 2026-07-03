package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // ★ 한 차시(Content)당 보통 세트 1개지만, 여러 개 허용될 수도 있어 List로 둠
    List<Quiz> findByContentIdOrderByIdAsc(Long contentId);

    Optional<Quiz> findFirstByContentIdOrderByIdAsc(Long contentId);

    // ★ 카테고리 목록 화면에서 단원별 문항 수 뱃지에 사용 (세트가 아니라 문항 총합 기준)
    @org.springframework.data.jpa.repository.Query(
            "select count(q) from QuizQuestion q where q.quiz.content.category.id = :categoryId"
    )
    long countQuestionsByCategoryId(Long categoryId);

    // ★ /quiz 목록 화면에서 카테고리로 필터링할 때 사용
    List<Quiz> findByContent_Category_Id(Long categoryId);
}