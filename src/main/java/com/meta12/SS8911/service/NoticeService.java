package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.NoticeDTO;
import com.meta12.SS8911.entity.Notice;
import com.meta12.SS8911.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository repository;

    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticePage(int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "id"));
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public NoticeDTO getNotice(Long id) {
        Notice notice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        return toDTO(notice);
    }

    // 작성 권한 체크
    public void createNotice(NoticeDTO dto) {
        // 전달받은 dto의 author(현재 로그인 유저)가 admin777인지 확인
        if (dto.getAuthor() == null || !"admin888".equals(dto.getAuthor().getUsername())) {
            throw new RuntimeException("관리자만 작성할 수 있습니다.");
        }
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setAuthor(dto.getAuthor());
        notice.setCategory(dto.getCategory());
        repository.save(notice);
    }

    // 수정 권한 체크
    public void updateNotice(Long id, NoticeDTO dto) {
        Notice notice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        // [수정 포인트] notice.getAuthor()(기존 작성자)가 아니라
        // dto.getAuthor()(현재 수정을 시도하는 로그인 유저)가 admin777인지 확인해야 합니다.
        if (dto.getAuthor() == null || !"admin888".equals(dto.getAuthor().getUsername())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setCategory(dto.getCategory());
        // JPA의 변경 감지(Dirty Checking)로 인해 repository.save()를 안 호출해도 업데이트됩니다.
    }

    // 삭제 권한 체크
    public void deleteNotice(Long id) {
        // [참고] 삭제는 컨트롤러에서 이미 admin777인지 체크하므로
        // 서비스에서는 존재 여부만 확인하고 삭제해도 무방합니다.
        Notice notice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 게시글이 없습니다."));

        repository.delete(notice);
    }

    private NoticeDTO toDTO(Notice notice) {
        NoticeDTO dto = new NoticeDTO();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setAuthor(notice.getAuthor());
        dto.setCreatedAt(notice.getCreatedAt());
        dto.setCategory(notice.getCategory());
        return dto;
    }
}