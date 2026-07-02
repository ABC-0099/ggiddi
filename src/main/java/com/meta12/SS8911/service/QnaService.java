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
}