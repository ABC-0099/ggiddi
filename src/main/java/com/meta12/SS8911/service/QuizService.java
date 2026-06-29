package com.meta12.SS8911.service;

import com.meta12.SS8911.entity.Quiz;
import com.meta12.SS8911.entity.QuizBox;
import com.meta12.SS8911.entity.QuizUnit;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.QuizBoxRepository;
import com.meta12.SS8911.repository.QuizRepository;
import com.meta12.SS8911.repository.QuizUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizUnitRepository quizUnitRepository;
    private final QuizBoxRepository quizBoxRepository;

    // 퀴즈 단건 조회 (문항 포함)
    public Quiz getQuiz(Long id) {
        return quizRepository.findByIdWithUnits(id)
                .orElseThrow(() -> new RuntimeException("퀴즈 없음"));
    }

    // 잠금 해제된 퀴즈 목록
    public List<Quiz> getUnlockedQuizzes() {
        return quizRepository.findByUnlockedTrue();
    }

    // 퀴즈에 속한 문항 순서대로 조회
    public List<QuizUnit> getUnits(Quiz quiz) {
        return quizUnitRepository.findByQuizOrderByUnitOrderAsc(quiz);
    }

    // 퀴즈 채점 및 결과 저장
    public QuizBox submit(Long quizId, Map<Long, String> answers, SiteUser user) {
        Quiz quiz = getQuiz(quizId);
        List<QuizUnit> units = getUnits(quiz);

        int score = 0;
        for (QuizUnit unit : units) {
            String submitted = answers.get(unit.getId());
            if (unit.getAnswer().equals(submitted)) {
                score++;
            }
        }

        // 기존 결과 있으면 덮어쓰기, 없으면 새로 저장
        QuizBox box = quizBoxRepository.findByQuizAndUser(quiz, user)
                .orElse(new QuizBox());
        box.setQuiz(quiz);
        box.setUser(user);
        box.setScore(score);
        box.setTotal(units.size());
        box.setSolvedDate(LocalDateTime.now());
        quizBoxRepository.save(box);

        return box;
    }

    // 사용자의 특정 퀴즈 결과 조회
    public QuizBox getResult(Quiz quiz, SiteUser user) {
        return quizBoxRepository.findByQuizAndUser(quiz, user).orElse(null);
    }

    // 사용자의 전체 퀴즈 결과 조회
    public List<QuizBox> getAllResults(SiteUser user) {
        return quizBoxRepository.findByUserOrderBySolvedDateDesc(user);
    }
}
