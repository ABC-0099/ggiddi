package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.QnaDTO;
import com.meta12.SS8911.config.InquiryStatus;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;

    @Transactional
    public void create(QnaDTO dto, SiteUser author) {
        Qna qna = new Qna();
        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
        qna.setAuthor(author);
        qna.setStatus(InquiryStatus.PENDING);
        qna.setCreatedDate(LocalDateTime.now());

        qna.setCategory(dto.getCategory());

        qnaRepository.save(qna);
    }

    public Qna getQna(Long id) {
        return qnaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의 없음"));
    }

    public Page<Qna> getMyQnas(SiteUser author, Pageable pageable) {
        return qnaRepository.findByAuthorOrderByCreatedDateDesc(author, pageable);
    }

    public Page<Qna> getAllQnas(Pageable pageable) {
        return qnaRepository.findAllByOrderByCreatedDateDesc(pageable);
    }

    @Transactional
    public void answer(Long id, String answerContent, SiteUser admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 답변할 수 있습니다.");
        }
        Qna qna = getQna(id);
        qna.setAnswer(answerContent);
        qna.setStatus(InquiryStatus.ANSWERED);
        qna.setAnsweredDate(LocalDateTime.now());
        qnaRepository.save(qna);
    }

    public void checkViewPermission(Qna qna, SiteUser user) {
        boolean isAuthor = qna.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }

    // 수정/삭제 권한: 작성자 본인 + 아직 답변 안 된 상태(PENDING)일 때만
    public void checkEditDeletePermission(Qna qna, SiteUser user) {
        boolean isAuthor = qna.getAuthor().getId().equals(user.getId());
        if (!isAuthor) {
            throw new RuntimeException("작성자만 수정/삭제할 수 있습니다.");
        }
        if (qna.getStatus() == InquiryStatus.ANSWERED) {
            throw new RuntimeException("답변이 완료된 문의는 수정/삭제할 수 없습니다.");
        }
    }

    @Transactional
    public void update(Long id, QnaDTO dto, SiteUser user) {
        Qna qna = getQna(id);
        checkEditDeletePermission(qna, user);
        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
        qnaRepository.save(qna);
    }

    @Transactional
    public void delete(Long id, SiteUser user) {
        Qna qna = getQna(id);
        checkEditDeletePermission(qna, user);
        qnaRepository.delete(qna);
    }
}