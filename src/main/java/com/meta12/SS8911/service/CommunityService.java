package com.meta12.SS8911.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityFileRepository communityFileRepository;
    private final Cloudinary cloudinary; // ← UPLOAD_DIR/URL_PREFIX 대신 이걸로 업로드

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

        // 이미지 저장 (Cloudinary)
        saveFiles(dto.getImageFiles(), community, "IMAGE");
        // 첨부파일 저장 (Cloudinary)
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
                    deleteFromCloudinary(f.getSavedPath(), f.getFileType());
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

        // 첨부파일 실제 파일도 같이 삭제 (Cloudinary)
        List<CommunityFile> files = communityFileRepository.findByCommunity(community);
        for (CommunityFile f : files) {
            deleteFromCloudinary(f.getSavedPath(), f.getFileType());
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

    // ── 파일 저장 공통 로직 (로컬 저장 대신 Cloudinary 업로드) ──
    private void saveFiles(List<MultipartFile> files, Community community, String fileType) {
        if (files == null) return;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {
                // IMAGE는 image, ATTACH(문서 등)는 raw로 업로드
                String resourceType = "IMAGE".equals(fileType) ? "image" : "raw";

                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("resource_type", resourceType)
                );
                String secureUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                CommunityFile cf = new CommunityFile();
                cf.setCommunity(community);
                cf.setOriginalName(file.getOriginalFilename());
                cf.setSavedPath(secureUrl); // 이제 로컬 경로가 아니라 Cloudinary URL 통째로 저장
                cf.setFileType(fileType);
                cf.setFileSize(file.getSize());
                communityFileRepository.save(cf);

                // 삭제 시 필요하므로 public_id도 같이 남겨두고 싶다면 CommunityFile에 필드 추가 권장
                // (지금은 URL에서 역산하는 대신 로그로만 남김)
                System.out.println("[Cloudinary] uploaded publicId=" + publicId);

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
            }
        }
    }

    // 로컬 파일 삭제 대신 Cloudinary에서 삭제.
    // public_id를 따로 저장 안 해서 URL에서 역추출 (실패해도 DB 레코드는 정상 삭제되도록 예외를 삼킴)
    private void deleteFromCloudinary(String secureUrl, String fileType) {
        try {
            if (secureUrl == null) return;
            String resourceType = "IMAGE".equals(fileType) ? "image" : "raw";

            // 예: https://res.cloudinary.com/xxx/image/upload/v12345/abcdefg.png
            // → public_id는 "abcdefg" (버전/확장자 제거)
            String afterUpload = secureUrl.substring(secureUrl.indexOf("/upload/") + "/upload/".length());
            if (afterUpload.startsWith("v")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }
            String publicId = afterUpload.contains(".")
                    ? afterUpload.substring(0, afterUpload.lastIndexOf('.'))
                    : afterUpload;

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (Exception e) {
            // Cloudinary 삭제 실패해도 게시글/DB 삭제 흐름은 막지 않음
            System.err.println("Cloudinary 파일 삭제 실패: " + secureUrl + " (" + e.getMessage() + ")");
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