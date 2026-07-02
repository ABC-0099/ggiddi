package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.CommunityDTO;
import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.CommunityFile;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.CommunityFileRepository;
import com.meta12.SS8911.repository.CommunityRepository;
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
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityFileRepository communityFileRepository;

    // 파일 저장 경로 (프로젝트 환경에 맞게 수정)
    private static final String UPLOAD_DIR = "C:/meta12/SS8911/uploads/community/";
    private static final String URL_PREFIX = "/uploads/community/";   // ★ 이 줄 추가

    public List<Community> getCommunityPosts(Category category, String sort, String kw) {
        boolean hasKeyword = kw != null && !kw.isBlank();

        if (hasKeyword) {
            if (category == null || category == Category.ALL) {
                return communityRepository.findAllByKeyword(kw);
            }
            return communityRepository.findByCategoryAndKeyword(category, kw);
        }

        boolean oldest = "oldest".equals(sort);
        if (category == null || category == Category.ALL) {
            return oldest ? communityRepository.findAllWithAuthorOldest()
                    : communityRepository.findAllWithAuthor();
        }
        return oldest ? communityRepository.findByCategoryWithAuthorOldest(category)
                : communityRepository.findByCategoryWithAuthor(category);
    }

    @Transactional
    public void create(CommunityDTO dto, SiteUser author) {
        Community community = new Community();
        community.setTitle(dto.getTitle());
        community.setContent(dto.getContent());
        community.setCategory(dto.getCategory());
        community.setAuthor(author);
        community.setCreatedDate(LocalDateTime.now());
        communityRepository.save(community);

        // 이미지 저장
        saveFiles(dto.getImageFiles(), community, "IMAGE");
        // 첨부파일 저장
        saveFiles(dto.getAttachFiles(), community, "ATTACH");
    }

    public Community getPost(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
    }

    public List<Community> getPostsByAuthor(SiteUser author) {
        return communityRepository.findByAuthorOrderByCreatedDateDesc(author);
    }

    public Page<Community> getPostsByAuthor(SiteUser author, Pageable pageable) {
        return communityRepository.findByAuthorOrderByCreatedDateDesc(author, pageable);
    }

    public List<CommunityFile> getFiles(Community community) {
        return communityFileRepository.findByCommunity(community);
    }

    // 수정
    @Transactional
    public void update(Long id, CommunityDTO dto, SiteUser user) {
        Community community = getPost(id);
        checkEditPermission(community, user);
        community.setTitle(dto.getTitle());
        community.setContent(dto.getContent());
        community.setCategory(dto.getCategory());
        communityRepository.save(community);

        // 삭제 요청된 기존 파일 제거
        if (dto.getDeleteFileIds() != null && !dto.getDeleteFileIds().isEmpty()) {
            for (Long fileId : dto.getDeleteFileIds()) {
                communityFileRepository.findById(fileId).ifPresent(f -> {
                    deletePhysicalFile(f.getSavedPath());
                    communityFileRepository.delete(f);
                });
            }
        }

        // 새로 추가된 파일 저장
        saveFiles(dto.getImageFiles(), community, "IMAGE");
        saveFiles(dto.getAttachFiles(), community, "ATTACH");
    }

    // 삭제
    @Transactional
    public void delete(Long id, SiteUser user) {
        Community community = getPost(id);
        checkDeletePermission(community, user);

        // 첨부파일 실제 파일도 같이 삭제
        List<CommunityFile> files = communityFileRepository.findByCommunity(community);
        for (CommunityFile f : files) {
            deletePhysicalFile(f.getSavedPath());
        }
        communityFileRepository.deleteByCommunity(community);

        communityRepository.delete(community);
    }

    // 작성자 또는 관리자 권한 체크
    private void checkEditPermission(Community community, SiteUser user) {
        if (!community.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }
    }

    private void checkDeletePermission(Community community, SiteUser user) {
        boolean isAuthor = community.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }

    // ── 파일 저장 공통 로직 ──
    private void saveFiles(List<MultipartFile> files, Community community, String fileType) {
        if (files == null) return;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {
                // 저장 디렉토리 없으면 생성
                Path dirPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                // 중복 방지용 UUID 파일명
                String originalName = file.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String savedName = UUID.randomUUID() + ext;
                Path savedFullPath = dirPath.resolve(savedName);

                file.transferTo(savedFullPath);

                CommunityFile cf = new CommunityFile();
                cf.setCommunity(community);
                cf.setOriginalName(originalName);
                cf.setSavedPath(URL_PREFIX + savedName);
                cf.setFileType(fileType);
                cf.setFileSize(file.getSize());
                communityFileRepository.save(cf);

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
            }
        }
    }

    private void deletePhysicalFile(String savedPath) {
        try {
            String fileName = savedPath.substring(savedPath.lastIndexOf('/') + 1);
            Path path = Paths.get(UPLOAD_DIR, fileName);
            Files.deleteIfExists(path);
        } catch (Exception e) {
            System.err.println("파일 삭제 실패: " + savedPath);
        }
    }

    public Page<Community> getCommunityPosts(Category category, String sort, String kw, Pageable pageable) {
        boolean hasKeyword = kw != null && !kw.isBlank();

        if (hasKeyword) {
            if (category == null || category == Category.ALL) {
                return communityRepository.findAllByKeyword(kw, pageable);
            }
            return communityRepository.findByCategoryAndKeyword(category, kw, pageable);
        }

        boolean oldest = "oldest".equals(sort);
        if (category == null || category == Category.ALL) {
            return oldest ? communityRepository.findAllWithAuthorOldest(pageable)
                    : communityRepository.findAllWithAuthor(pageable);
        }
        return oldest ? communityRepository.findByCategoryWithAuthorOldest(category, pageable)
                : communityRepository.findByCategoryWithAuthor(category, pageable);
    }
}