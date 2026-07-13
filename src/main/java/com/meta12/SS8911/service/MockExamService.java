package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.*;
import com.meta12.SS8911.entity.*;
import com.meta12.SS8911.repository.*;
import com.meta12.SS8911.config.SourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MockExamService {

    private final MockExamRepository mockExamRepository;
    private final MockExamQuestionRepository mockExamQuestionRepository;
    private final MockExamBoxRepository mockExamBoxRepository;
    private final CategoryRepository categoryRepository;
    private final SiteUserRepository siteUserRepository;
    private final WrongAnswerNoteRepository wrongAnswerNoteRepository; // ★ 오답노트 저장용

    /**
     * /practice/mock 목록용 - 테마(카테고리)별로 회차 그룹핑.
     */
    @Transactional(readOnly = true)
    public LinkedHashMap<String, List<MockExamListItemDTO>> getExamListForUser(String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<MockExam> exams = mockExamRepository.findAllByOrderByCategoryIdAscRoundAsc();

        LinkedHashMap<String, List<MockExamListItemDTO>> grouped = new LinkedHashMap<>();

        for (MockExam exam : exams) {
            int poolCount = exam.getQuestions().size();

            MockExamBox last = mockExamBoxRepository
                    .findTopByMockExamIdAndUserIdOrderBySolvedDateDesc(exam.getId(), user.getId())
                    .orElse(null);
            Integer lastScore = (last == null || last.getTotal() == 0)
                    ? null
                    : (int) Math.round(last.getScore() * 100.0 / last.getTotal());

            long attemptCount = mockExamBoxRepository.countByMockExamIdAndUserId(exam.getId(), user.getId());

            String categoryName = exam.getCategory().getTitle();

            MockExamListItemDTO item = MockExamListItemDTO.builder()
                    .id(exam.getId())
                    .title(exam.getTitle())
                    .round(exam.getRound())
                    .categoryName(categoryName)
                    .poolCount(poolCount)
                    .timeLimitMinutes(exam.getTimeLimitMinutes() == null ? 30 : exam.getTimeLimitMinutes())
                    .lastScore(lastScore)
                    .attemptCount((int) attemptCount)
                    .build();

            grouped.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(item);
        }

        return grouped;
    }

    @Transactional(readOnly = true)
    public MockExam getExamEntity(Long examId) {
        return mockExamRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모의고사입니다."));
    }

    /**
     * 모의고사 시작 - 등록된 문항 전체를 순서만 섞어서 반환 (랜덤 서브셋 아님, 전부 다 풀어야 함).
     */
    @Transactional(readOnly = true)
    public MockExamStartDTO startExam(Long examId) {
        MockExam exam = getExamEntity(examId);

        List<MockExamQuestion> all = new ArrayList<>(exam.getQuestions());
        Collections.shuffle(all);

        List<MockExamQuestionDTO> questions = all.stream()
                .map(MockExamQuestionDTO::from)
                .collect(Collectors.toList());

        return MockExamStartDTO.builder()
                .examId(exam.getId())
                .title(exam.getTitle())
                .timeLimitMinutes(exam.getTimeLimitMinutes() == null ? 30 : exam.getTimeLimitMinutes())
                .questions(questions)
                .build();
    }

    /**
     * 전체 답안 한번에 제출 → 채점 → MockExamBox에 기록. 문항별 리뷰까지 반환.
     */
    public MockExamResultDTO submitExam(MockExamSubmitDTO submit, String username) {
        MockExam exam = getExamEntity(submit.getExamId());
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        Map<Long, Integer> selectedByQuestionId = submit.getAnswers() == null
                ? Collections.emptyMap()
                : submit.getAnswers().stream()
                .collect(Collectors.toMap(
                        MockExamSubmitDTO.AnswerEntry::getQuestionId,
                        MockExamSubmitDTO.AnswerEntry::getSelectedOption,
                        (a, b) -> a
                ));

        // 제출된 답안에 포함된 문항들만 채점 대상 (매번 랜덤 출제된 문항 기준)
        List<Long> questionIds = new ArrayList<>(selectedByQuestionId.keySet());
        List<MockExamQuestion> questions = mockExamQuestionRepository.findAllById(questionIds);

        int score = 0;
        List<MockExamResultDTO.QuestionResult> results = new ArrayList<>();

        for (MockExamQuestion q : questions) {
            Integer selected = selectedByQuestionId.get(q.getId());
            boolean correct = selected != null && selected.equals(q.getAnswer());
            if (correct) {
                score++;
            } else {
                saveWrongAnswer(exam, q, selected, user);
            }

            results.add(MockExamResultDTO.QuestionResult.builder()
                    .questionId(q.getId())
                    .question(q.getQuestion())
                    .options(List.of(q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()))
                    .selectedOption(selected)
                    .correctAnswer(q.getAnswer())
                    .correct(correct)
                    .explanation(q.getExplanation())
                    .build());
        }

        int total = questions.size();

        MockExamBox box = MockExamBox.builder()
                .mockExam(exam)
                .user(user)
                .score(score)
                .total(total)
                .elapsedSeconds(submit.getElapsedSeconds())
                .solvedDate(LocalDateTime.now())
                .build();
        mockExamBoxRepository.save(box);

        return MockExamResultDTO.builder()
                .examId(exam.getId())
                .score(score)
                .total(total)
                .questionResults(results)
                .build();
    }

    /**
     * 모의고사 오답을 스냅샷으로 저장 (원본 문항이 나중에 수정/삭제돼도 내용은 그대로 보존됨).
     */
    private void saveWrongAnswer(MockExam exam, MockExamQuestion q, Integer selected, SiteUser user) {
        WrongAnswerNote note = new WrongAnswerNote();
        note.setSiteUser(user);
        note.setSourceType(SourceType.MOCK_EXAM);
        note.setSourceQuestionId(q.getId());
        note.setQuestionText(q.getQuestion());
        note.setUserAnswer(optionText(q, selected));
        note.setCorrectAnswer(optionText(q, q.getAnswer()));
        note.setExplanation(q.getExplanation());
        note.setQuizSetId(exam.getId());
        note.setQuizSetTitle(exam.getTitle());
        note.setCategory(exam.getCategory().getTitle());

        wrongAnswerNoteRepository.save(note);
    }

    // 선택 번호(1~4)를 실제 보기 텍스트로 변환 (null이면 미답)
    private String optionText(MockExamQuestion q, Integer optionNumber) {
        if (optionNumber == null) return null;
        return switch (optionNumber) {
            case 1 -> q.getOption1();
            case 2 -> q.getOption2();
            case 3 -> q.getOption3();
            case 4 -> q.getOption4();
            default -> null;
        };
    }

    /**
     * 배움터 히어로 통계용 - [0]=응시 횟수, [1]=평균 정답률(%)
     */
    @Transactional(readOnly = true)
    public int[] getUserExamStats(String username) {
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        long count = mockExamBoxRepository.countByUserId(user.getId());
        Double avg = mockExamBoxRepository.findAvgAccuracyByUserId(user.getId());
        int avgPct = avg == null ? 0 : (int) Math.round(avg * 100);
        return new int[]{(int) count, avgPct};
    }

    @Transactional(readOnly = true)
    public long getTotalExamQuestionPoolCount() {
        return mockExamQuestionRepository.count();
    }

    // ───────── 관리자 CRUD ─────────

    @Transactional(readOnly = true)
    public List<MockExamAdminDTO> getAllForAdmin() {
        return mockExamRepository.findAllByOrderByCategoryIdAscRoundAsc()
                .stream()
                .map(MockExamAdminDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MockExamAdminDTO getForAdmin(Long id) {
        return MockExamAdminDTO.from(getExamEntity(id));
    }

    public MockExamAdminDTO create(MockExamAdminDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        MockExam exam = MockExam.builder()
                .category(category)
                .title(dto.getTitle())
                .round(dto.getRound())
                .timeLimitMinutes(dto.getTimeLimitMinutes())
                .build();

        if (dto.getQuestions() != null) {
            dto.getQuestions().forEach(qDto -> exam.addQuestion(
                    MockExamQuestion.builder()
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

        return MockExamAdminDTO.from(mockExamRepository.save(exam));
    }

    public MockExamAdminDTO update(Long id, MockExamAdminDTO dto) {
        MockExam exam = getExamEntity(id);

        exam.setTitle(dto.getTitle());
        exam.setRound(dto.getRound());
        exam.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        exam.getQuestions().clear();

        if (dto.getQuestions() != null) {
            dto.getQuestions().forEach(qDto -> exam.addQuestion(
                    MockExamQuestion.builder()
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

        return MockExamAdminDTO.from(exam);
    }

    public void delete(Long id) {
        mockExamRepository.deleteById(id);
    }
}