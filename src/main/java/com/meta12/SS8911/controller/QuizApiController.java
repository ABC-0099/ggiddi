package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.QuizAnswerCheckRequestDTO;
import com.meta12.SS8911.dto.QuizAnswerCheckResultDTO;
import com.meta12.SS8911.dto.QuizFinishRequestDTO;
import com.meta12.SS8911.dto.QuizStartDTO;
import com.meta12.SS8911.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizApiController {

    private final QuizService quizService;

    /**
     * 퀴즈 시작 - 문제은행에서 매번 랜덤으로 N문항 뽑아서 반환 (정답 미포함).
     */
    @GetMapping("/{quizId}/start")
    public QuizStartDTO start(@PathVariable Long quizId, Authentication authentication) {
        return quizService.startQuiz(quizId);
    }

    /**
     * 영상 시청 완료 후 강의 페이지에 임베드되는 퀴즈 - 문제은행에서 questionCount만큼만 랜덤 추출.
     */
    @GetMapping("/{quizId}/start-embedded")
    public QuizStartDTO startEmbedded(@PathVariable Long quizId, Authentication authentication) {
        return quizService.startQuizRandom(quizId);
    }

    /**
     * 문항 하나 채점 - 고르는 즉시 정답/오답 + 해설 반환.
     */
    @PostMapping("/question/{questionId}/check")
    public QuizAnswerCheckResultDTO check(@PathVariable Long questionId,
                                          @RequestBody QuizAnswerCheckRequestDTO req) {
        return quizService.checkAnswer(questionId, req.getSelectedOption());
    }

    /**
     * 퀴즈 완주 - 집계된 점수를 QuizBox에 기록.
     */
    @PostMapping("/{quizId}/finish")
    public void finish(@PathVariable Long quizId,
                       @RequestBody QuizFinishRequestDTO req,
                       Authentication authentication) {
        quizService.finishQuiz(quizId, req.getScore(), req.getTotal(), authentication.getName());
    }
}