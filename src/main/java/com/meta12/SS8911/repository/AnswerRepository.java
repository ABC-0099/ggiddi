package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Answer;
import com.meta12.SS8911.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQnaOrderByCreatedDateAsc(Qna qna);
}