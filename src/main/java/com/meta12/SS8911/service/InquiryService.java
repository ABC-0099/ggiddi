package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.InquiryDTO;
import com.meta12.SS8911.config.InquiryStatus;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Inquiry;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public void create(InquiryDTO dto, SiteUser author) {
        Inquiry inquiry = new Inquiry();
        inquiry.setTitle(dto.getTitle());
        inquiry.setContent(dto.getContent());
        inquiry.setAuthor(author);
        inquiry.setStatus(InquiryStatus.PENDING);
        inquiry.setCreatedDate(LocalDateTime.now());
        inquiryRepository.save(inquiry);
    }

    public Inquiry getInquiry(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의 없음"));
    }

    public Page<Inquiry> getMyInquiries(SiteUser author, Pageable pageable) {
        return inquiryRepository.findByAuthorOrderByCreatedDateDesc(author, pageable);
    }

    public Page<Inquiry> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAllByOrderByCreatedDateDesc(pageable);
    }

    @Transactional
    public void answer(Long id, String answerContent, SiteUser admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 답변할 수 있습니다.");
        }
        Inquiry inquiry = getInquiry(id);
        inquiry.setAnswer(answerContent);
        inquiry.setStatus(InquiryStatus.ANSWERED);
        inquiry.setAnsweredDate(LocalDateTime.now());
        inquiryRepository.save(inquiry);
    }

    // 조회 권한: 작성자 본인 또는 관리자만
    public void checkViewPermission(Inquiry inquiry, SiteUser user) {
        boolean isAuthor = inquiry.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }
}