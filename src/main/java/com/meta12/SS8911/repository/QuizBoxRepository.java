package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.QuizBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizBoxRepository extends JpaRepository<QuizBox, Long> {

    List<QuizBox> findByUserIdOrderBySolvedDateDesc(Long userId);

    long countByUserId(Long userId);

    // ★ 잠금 해제 체크용 - 이전 차시 퀴즈를 한 번이라도 풀었는지
    boolean existsByQuizIdAndUserId(Long quizId, Long userId);

    // ★ "이전 풀이 결과" 표시용 - 가장 최근 풀이 1건
    java.util.Optional<QuizBox> findTopByQuizIdAndUserIdOrderBySolvedDateDesc(Long quizId, Long userId);

    // ★ 배움터 히어로의 "평균 정답률" 계산용 (score/total 평균을 백분율로)
    @Query("select coalesce(avg(cast(b.score as double) / b.total), 0) " +
            "from QuizBox b where b.user.id = :userId and b.total > 0")
    Double findAvgAccuracyByUserId(Long userId);
}