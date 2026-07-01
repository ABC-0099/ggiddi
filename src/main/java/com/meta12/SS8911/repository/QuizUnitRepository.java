package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.QuizUnit;
import com.meta12.SS8911.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizUnitRepository extends JpaRepository<QuizUnit, Long> {

    // 퀴즈에 속한 문항 순서대로 조회
    List<QuizUnit> findByQuizOrderByUnitOrderAsc(Quiz quiz);
}
