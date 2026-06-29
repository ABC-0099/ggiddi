package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.QuizBox;
import com.meta12.SS8911.entity.Quiz;
import com.meta12.SS8911.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizBoxRepository extends JpaRepository<QuizBox, Long> {

    // 특정 사용자의 특정 퀴즈 결과 조회
    Optional<QuizBox> findByQuizAndUser(Quiz quiz, SiteUser user);

    // 특정 사용자의 전체 퀴즈 결과 조회
    List<QuizBox> findByUserOrderBySolvedDateDesc(SiteUser user);
}
