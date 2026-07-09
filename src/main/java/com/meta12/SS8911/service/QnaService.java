package com.meta12.SS8911.service;

import com.meta12.SS8911.config.QnaCategory;
import com.meta12.SS8911.dto.QnaDTO;
import com.meta12.SS8911.config.InquiryStatus;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.QnaFile;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.QnaFileRepository;
import com.meta12.SS8911.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaFileRepository qnaFileRepository;

    private static final String UPLOAD_DIR = "C:/meta12/SS8911/uploads/qna/";
    private static final String URL_PREFIX = "/uploads/qna/";

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

        saveFiles(dto.getImages(), qna);
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

    // 관리자 마이페이지: 답변대기만 모아보기
    public Page<Qna> getPendingQnas(Pageable pageable) {
        return qnaRepository.findByStatusOrderByCreatedDateDesc(InquiryStatus.PENDING, pageable);
    }

    // 게시판 관리 탭 배지: 미답변 개수
    public long countPending() {
        return qnaRepository.countByStatus(InquiryStatus.PENDING);
    }

    // ※ answer()는 여기서 삭제되었습니다. AnswerService.create()로 이동했어요.

    public void checkViewPermission(Qna qna, SiteUser user) {
        boolean isAuthor = qna.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }

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
        qna.setCategory(dto.getCategory());
        qnaRepository.save(qna);

        if (dto.getDeleteFileIds() != null && !dto.getDeleteFileIds().isEmpty()) {
            for (Long fileId : dto.getDeleteFileIds()) {
                qnaFileRepository.findById(fileId).ifPresent(f -> {
                    deletePhysicalFile(f.getSavedPath());
                    qnaFileRepository.delete(f);
                });
            }
        }

        saveFiles(dto.getImages(), qna);
    }

    @Transactional
    public void delete(Long id, SiteUser user) {
        Qna qna = getQna(id);
        checkEditDeletePermission(qna, user);

        List<QnaFile> files = qnaFileRepository.findByQna(qna);
        for (QnaFile f : files) {
            deletePhysicalFile(f.getSavedPath());
        }
        qnaFileRepository.deleteByQna(qna);

        qnaRepository.delete(qna);
    }

    // 게시판 관리: 관리자는 작성자/답변상태 상관없이 삭제 가능 (신고글 등 처리용)
    @Transactional
    public void adminDelete(Long id, SiteUser admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 삭제할 수 있습니다.");
        }
        Qna qna = getQna(id);

        List<QnaFile> files = qnaFileRepository.findByQna(qna);
        for (QnaFile f : files) {
            deletePhysicalFile(f.getSavedPath());
        }
        qnaFileRepository.deleteByQna(qna);

        qnaRepository.delete(qna);
    }

    // ── 파일 저장 공통 로직 ──
    private void saveFiles(List<MultipartFile> files, Qna qna) {
        if (files == null) return;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {
                Path dirPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                String originalName = file.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String savedName = UUID.randomUUID() + ext;
                Path savedFullPath = dirPath.resolve(savedName);

                file.transferTo(savedFullPath);

                QnaFile qf = new QnaFile();
                qf.setQna(qna);
                qf.setOriginalName(originalName);
                qf.setSavedPath(URL_PREFIX + savedName);
                qf.setFileSize(file.getSize());
                qnaFileRepository.save(qf);

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
            }
        }
    }

// ... 상단 패키지, 임포트 및 기존 원본 코드들 (create, update, delete, saveFiles 등) 전부 그대로 유지 ...

    private void deletePhysicalFile(String savedPath) {
        try {
            String fileName = savedPath.substring(savedPath.lastIndexOf('/') + 1);
            Path path = Paths.get(UPLOAD_DIR, fileName);
            Files.deleteIfExists(path);
        } catch (Exception e) {
            System.err.println("파일 삭제 실패: " + savedPath);
        }
    }

    // ── 💡 보유하신 QnaCategory Enum 구조에 맞춰 최종 보완된 메서드 ──
    public Page<Qna> getAdminBoardList(String categoryStr, String kw, Pageable pageable) {
        if (kw == null) {
            kw = "";
        }

        // "전체" 탭: 카테고리 필터 없이 keyword로만 검색
        if (categoryStr == null || categoryStr.equalsIgnoreCase("all")) {
            return qnaRepository.findByKeyword(kw, pageable);   // ← Repository에 이미 있던 메서드 사용
        }

        com.meta12.SS8911.config.QnaCategory category;
        try {
            category = com.meta12.SS8911.config.QnaCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            category = com.meta12.SS8911.config.QnaCategory.ACCOUNT;
        }

        return qnaRepository.findByCategoryAndKeyword(category, kw, pageable);
    }

}