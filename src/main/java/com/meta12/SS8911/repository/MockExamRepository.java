package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.MockExam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockExamRepository extends JpaRepository<MockExam, Long> {

    List<MockExam> findByCategoryIdOrderByRoundAsc(Long categoryId);

    List<MockExam> findAllByOrderByCategoryIdAscRoundAsc();
}