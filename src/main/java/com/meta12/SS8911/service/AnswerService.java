package com.meta12.SS8911.service;

import com.meta12.SS8911.config.InquiryStatus;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Answer;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.AnswerRepository;
import com.meta12.SS8911.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QnaRepository qnaRepository;

    // ===================== 답변 작성 (관리자) =====================
    public Answer create(Long qnaId, String content, SiteUser admin) {
        checkAdmin(admin);

        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("문의글이 존재하지 않습니다."));

        Answer answer = new Answer();
        answer.setQna(qna);
        answer.setAdmin(admin);
        answer.setContent(content);
        answer.setCreatedDate(LocalDateTime.now());
        Answer saved = answerRepository.save(answer);

        qna.setStatus(InquiryStatus.ANSWERED);
        qnaRepository.save(qna);

        return saved;
    }

    // ===================== 답변 수정 (관리자) =====================
    public Answer update(Long answerId, String content, SiteUser admin) {
        checkAdmin(admin);

        Answer answer = getAnswer(answerId);
        answer.setContent(content);
        return answerRepository.save(answer);
    }

    // ===================== 답변 삭제 (관리자) =====================
    public void delete(Long answerId, SiteUser admin) {
        checkAdmin(admin);

        Answer answer = getAnswer(answerId);
        Qna qna = answer.getQna();

        answerRepository.delete(answer);

        // 남은 답변이 없으면 다시 답변대기로, 있으면 답변완료 유지
        List<Answer> remaining = answerRepository.findByQnaOrderByCreatedDateAsc(qna);
        qna.setStatus(remaining.isEmpty() ? InquiryStatus.PENDING : InquiryStatus.ANSWERED);
        qnaRepository.save(qna);
    }

    // ===================== 조회 =====================
    public Answer getAnswer(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));
    }

    public List<Answer> getByQna(Qna qna) {
        return answerRepository.findByQnaOrderByCreatedDateAsc(qna);
    }

    // ===================== 권한 체크 =====================
    private void checkAdmin(SiteUser user) {
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 답변을 작성/수정/삭제할 수 있습니다.");
        }
    }
}