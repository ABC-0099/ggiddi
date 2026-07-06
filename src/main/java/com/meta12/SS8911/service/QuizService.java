package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.*;
import com.meta12.SS8911.entity.*;
import com.meta12.SS8911.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizBoxRepository quizBoxRepository;
    private final ContentRepository contentRepository;
    private final SiteUserRepository siteUserRepository;
    private final ProgressRepository progressRepository;

    /**
     * 특정 강의(콘텐츠)의 연습퀴즈 세트 (학습자용, 정답 미포함).
     * 한 차시에 세트가 여러 개 있을 수 있으므로 가장 먼저 등록된 세트를 기본으로 반환.
     */
    @Transactional(readOnly = true)
    public QuizDTO getQuizForContent(Long contentId) {
        Quiz quiz = quizRepository.findFirstByContentIdOrderByIdAsc(contentId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 연습퀴즈가 없습니다."));
        return QuizDTO.from(quiz);
    }

    /**
     * 퀴즈 풀이 화면에 필요한 Quiz 엔티티 원본 조회 (잠금 체크 등에 사용).
     */
    @Transactional(readOnly = true)
    public Quiz getQuizEntity(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));
    }

    /**
     * 강의 시청 페이지(content/view)에서 이 강의(Content)에 연결된 퀴즈를 찾을 때 사용.
     * 퀴즈가 아직 등록 안 된 강의도 있을 수 있어서 null-safe.
     */
    @Transactional(readOnly = true)
    public Quiz getQuizEntityForContent(Long contentId) {
        return quizRepository.findFirstByContentIdOrderByIdAsc(contentId).orElse(null);
    }

    /**
     * 잠금 여부 판단 - 두 조건을 모두 만족해야 열림.
     * 1) 이 차시(Content) 영상을 끝까지 봤는지 (Progress.completed)
     * 2) 이전 차시 퀴즈를 완료(한 번이라도 풀이)했는지 - 카테고리 내 첫 차시면 통과
     */
    @Transactional(readOnly = true)
    public boolean isUnlocked(Quiz quiz, String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        Content content = quiz.getContent();

        // ★ 1) 이 차시 영상을 끝까지 봤는지 체크
        boolean videoCompleted = progressRepository
                .findBySiteUserAndContent(user, content)
                .map(Progress::isCompleted)
                .orElse(false);

        if (!videoCompleted) {
            return false; // 영상을 안 봤으면 이전 차시 여부와 상관없이 무조건 잠김
        }

        // ★ 2) 이전 차시 퀴즈 완료 여부 체크. id가 아니라 Content.sequence(강의 순서)로 판단.
        Content previous = contentRepository
                .findTopByCategoryIdAndSequenceLessThanOrderBySequenceDesc(content.getCategory().getId(), content.getSequence())
                .orElse(null);

        if (previous == null) {
            return true; // 카테고리 내 첫 차시는 (영상만 다 봤으면) 항상 열림
        }

        Quiz previousQuiz = quizRepository.findFirstByContentIdOrderByIdAsc(previous.getId()).orElse(null);
        if (previousQuiz == null) {
            return true; // 이전 차시에 퀴즈가 아예 없으면 잠글 이유 없음
        }

        return quizBoxRepository.existsByQuizIdAndUserId(previousQuiz.getId(), user.getId());
    }

    /**
     * 이 퀴즈에 대한 사용자의 가장 최근 풀이 결과 (없으면 null).
     */
    @Transactional(readOnly = true)
    public QuizBox getLastResult(Long quizId, String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return quizBoxRepository.findTopByQuizIdAndUserIdOrderBySolvedDateDesc(quizId, user.getId())
                .orElse(null);
    }

    /**
     * /quiz 목록 화면용 - 이 유저가 볼 수 있는 모든 퀴즈 세트를 목록 카드 형태로.
     * categoryId가 있으면 그 카테고리 소속 강의의 퀴즈만 필터링.
     */
    @Transactional(readOnly = true)
    public java.util.LinkedHashMap<String, List<QuizListItemDTO>> getQuizListForUser(String username, Long categoryId) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Quiz> quizzes = (categoryId != null)
                ? quizRepository.findByContent_Category_Id(categoryId)
                : quizRepository.findAll();

        // ★ 카테고리(테마) id 오름차순으로 정렬 - 메인페이지 커리큘럼 순서(테마1~4)와 맞춤
        quizzes.sort(java.util.Comparator.comparing(q -> q.getContent().getCategory().getId()));

        java.util.LinkedHashMap<String, List<QuizListItemDTO>> grouped = new java.util.LinkedHashMap<>();

        for (Quiz quiz : quizzes) {
            int poolCount = quiz.getQuestions().size();
            int questionCount = effectiveQuestionCount(quiz);

            QuizBox last = quizBoxRepository
                    .findTopByQuizIdAndUserIdOrderBySolvedDateDesc(quiz.getId(), user.getId())
                    .orElse(null);
            Integer lastScore = (last == null || last.getTotal() == 0)
                    ? null
                    : (int) Math.round(last.getScore() * 100.0 / last.getTotal());

            String categoryName = quiz.getContent().getCategory().getTitle();

            QuizListItemDTO item = QuizListItemDTO.builder()
                    .id(quiz.getId())
                    .title(quiz.getTitle())
                    .contentTitle(quiz.getContent().getTitle())
                    .categoryName(categoryName)
                    .poolCount(poolCount)
                    .questionCount(questionCount)
                    .lastScore(lastScore)
                    .unlocked(true) // ★ 배움터 연습퀴즈 목록은 잠금 없이 바로 풀 수 있음
                    .build();

            grouped.computeIfAbsent(categoryName, k -> new java.util.ArrayList<>()).add(item);
        }

        return grouped;
    }

    /**
     * 사용자 전체 퀴즈 통계 (배움터/목록 상단 요약 박스용).
     * [0]=푼 횟수, [1]=평균 정답률(%)
     */
    @Transactional(readOnly = true)
    public int[] getUserQuizStats(String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        long count = quizBoxRepository.countByUserId(user.getId());
        Double avg = quizBoxRepository.findAvgAccuracyByUserId(user.getId());
        int avgPct = avg == null ? 0 : (int) Math.round(avg * 100);
        return new int[]{(int) count, avgPct};
    }

    /**
     * 문제은행 전체 문항 수 (사이트 전체 기준, 목록 화면 요약 박스용).
     */
    @Transactional(readOnly = true)
    public long getTotalQuestionPoolCount() {
        return quizQuestionRepository.count();
    }

    /**
     * 퀴즈 시작 - 문제은행(전체 문항)에서 questionCount 만큼 랜덤으로 뽑아서 반환 (정답 미포함).
     */
    @Transactional(readOnly = true)
    /**
     * 배움터 연습퀴즈 목록(/quiz)에서 들어왔을 때 - 전체 문항을 순서만 섞어서 다 보여줌.
     * (랜덤 N개만 뽑는 startQuizRandom()은 나중에 영상 재생 화면 임베드 퀴즈용으로 남겨둠)
     */
    public QuizStartDTO startQuiz(Long quizId) {
        Quiz quiz = getQuizEntity(quizId);

        List<QuizQuestion> pool = new java.util.ArrayList<>(quiz.getQuestions());
        java.util.Collections.shuffle(pool); // 순서만 매번 랜덤, 개수는 전체 다 보여줌

        List<QuizQuestionDTO> all = pool.stream()
                .map(QuizQuestionDTO::from)
                .collect(Collectors.toList());

        return QuizStartDTO.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .questions(all)
                .build();
    }

    /**
     * (미사용 - 추후 영상 재생 화면에서 "영상 끝나고 랜덤 N문항" 임베드 퀴즈용으로 예약)
     * 문제은행에서 questionCount 만큼만 랜덤으로 뽑아서 반환.
     */
    public QuizStartDTO startQuizRandom(Long quizId) {
        Quiz quiz = getQuizEntity(quizId);

        List<QuizQuestion> pool = new java.util.ArrayList<>(quiz.getQuestions());
        java.util.Collections.shuffle(pool);

        int pickCount = Math.min(effectiveQuestionCount(quiz), pool.size());
        List<QuizQuestionDTO> picked = pool.stream()
                .limit(pickCount)
                .map(QuizQuestionDTO::from)
                .collect(Collectors.toList());

        return QuizStartDTO.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .questions(picked)
                .build();
    }

    private int effectiveQuestionCount(Quiz quiz) {
        Integer configured = quiz.getQuestionCount();
        int poolSize = quiz.getQuestions().size();
        if (configured == null || configured <= 0 || configured > poolSize) {
            return poolSize; // 설정 안 했거나 전체보다 크면 전체 출제
        }
        return configured;
    }

    /**
     * 문항 하나 채점 - 선택하는 즉시 정답/해설을 바로 알려주는 방식.
     */
    @Transactional(readOnly = true)
    public QuizAnswerCheckResultDTO checkAnswer(Long questionId, Integer selectedOption) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문항입니다."));

        boolean correct = question.getAnswer().equals(selectedOption);

        return QuizAnswerCheckResultDTO.builder()
                .correct(correct)
                .correctAnswer(correct ? null : question.getAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    /**
     * 퀴즈 완주 - 클라이언트가 문항별 채점(checkAnswer)을 거쳐 집계한 최종 점수를 QuizBox에 기록.
     * (각 문항 채점 자체는 서버에서 이미 검증됐으므로, 여기서는 집계값만 저장)
     */
    public void finishQuiz(Long quizId, int score, int total, String username) {
        Quiz quiz = getQuizEntity(quizId);
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        QuizBox box = new QuizBox();
        box.setQuiz(quiz);
        box.setUser(user);
        box.setScore(score);
        box.setTotal(total);
        box.setSolvedDate(LocalDateTime.now());
        quizBoxRepository.save(box);
    }

    /**
     * 카테고리 전체 문항 수 (배움터 메인 카테고리 카드의 "N문항" 뱃지용).
     */
    @Transactional(readOnly = true)
    public long countQuestionsByCategory(Long categoryId) {
        return quizRepository.countQuestionsByCategoryId(categoryId);
    }

    /**
     * 배움터 히어로 통계 - 완료 퀴즈 수.
     */
    @Transactional(readOnly = true)
    public long countCompletedByUser(Long userId) {
        return quizBoxRepository.countByUserId(userId);
    }

    /**
     * 배움터 히어로 통계 - 평균 정답률 (%).
     */
    @Transactional(readOnly = true)
    public int getAvgAccuracyByUser(Long userId) {
        Double avg = quizBoxRepository.findAvgAccuracyByUserId(userId);
        return avg == null ? 0 : (int) Math.round(avg * 100);
    }

    // ───────── 관리자 CRUD ─────────

    @Transactional(readOnly = true)
    public List<QuizAdminDTO> getAllForAdmin() {
        return quizRepository.findAll()
                .stream()
                .map(QuizAdminDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizAdminDTO getForAdmin(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));
        return QuizAdminDTO.from(quiz);
    }

    /**
     * 세트 생성 + 문항 일괄 등록.
     */
    public QuizAdminDTO create(QuizAdminDTO dto) {
        Content content = contentRepository.findById(dto.getContentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        Quiz quiz = Quiz.builder()
                .content(content)
                .title(dto.getTitle())
                .questionCount(dto.getQuestionCount())
                .build();

        if (dto.getQuestions() != null) {
            dto.getQuestions().forEach(qDto -> quiz.addQuestion(
                    QuizQuestion.builder()
                            .question(qDto.getQuestion())
                            .questionType(qDto.getQuestionType())
                            .option1(qDto.getOption1())
                            .option2(qDto.getOption2())
                            .option3(qDto.getOption3())
                            .option4(qDto.getOption4())
                            .answer(qDto.getAnswer())
                            .explanation(qDto.getExplanation())
                            .build()
            ));
        }

        return QuizAdminDTO.from(quizRepository.save(quiz));
    }

    /**
     * 세트 제목 + 문항 전체 갱신 (기존 문항 삭제 후 재등록 - orphanRemoval 활용).
     */
    public QuizAdminDTO update(Long id, QuizAdminDTO dto) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));

        quiz.setTitle(dto.getTitle());
        quiz.setQuestionCount(dto.getQuestionCount());
        quiz.getQuestions().clear();

        if (dto.getQuestions() != null) {
            dto.getQuestions().forEach(qDto -> quiz.addQuestion(
                    QuizQuestion.builder()
                            .question(qDto.getQuestion())
                            .questionType(qDto.getQuestionType())
                            .option1(qDto.getOption1())
                            .option2(qDto.getOption2())
                            .option3(qDto.getOption3())
                            .option4(qDto.getOption4())
                            .answer(qDto.getAnswer())
                            .explanation(qDto.getExplanation())
                            .build()
            ));
        }

        return QuizAdminDTO.from(quiz);
    }

    public void delete(Long id) {
        quizRepository.deleteById(id);
    }
}