package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 퀴즈 + 문항 한번에 조회
    @Query("SELECT q FROM Quiz q JOIN FETCH q.units WHERE q.id = :id")
    Optional<Quiz> findByIdWithUnits(Long id);

    // 잠금 해제된 퀴즈 목록
    List<Quiz> findByUnlockedTrue();
}
