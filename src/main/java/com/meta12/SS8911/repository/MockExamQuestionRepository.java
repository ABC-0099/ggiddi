package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.MockExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockExamQuestionRepository extends JpaRepository<MockExamQuestion, Long> {

    List<MockExamQuestion> findByMockExamId(Long mockExamId);
}
