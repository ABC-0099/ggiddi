package com.meta12.SS8911.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.meta12.SS8911.dto.ContentDTO;
import com.meta12.SS8911.entity.*;
import com.meta12.SS8911.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final CategoryRepository categoryRepository;
    private final ProgressRepository progressRepository;
    private final OrderPayRepository orderPayRepository;
    private final SiteUserRepository siteUserRepository;
    private final Cloudinary cloudinary; // ← 로컬 uploadPath/thumbPath 대신 이걸로 업로드


    public List<Content> list(Long categoryId, SiteUser user) {
        List<Content> contentList = contentRepository.findByCategoryIdOrderBySequenceAsc(categoryId);

        for (Content content : contentList) {
            if (user != null) {
                // 🌟 수정: exists가 아니라 findBy로 데이터를 통째로 가져와야 합니다.
                Optional<Progress> progress = progressRepository.findBySiteUserAndContent(user, content);

                if (progress.isPresent()) {
                    // 0.5 이상이면 올림, 미만이면 내림 처리 (반올림)
                    int roundedPercent = (int) Math.round(progress.get().getPercentage());
                    content.setProgressPercent(roundedPercent);
                } else {
                    content.setProgressPercent(0);
                }
            } else {
                content.setProgressPercent(0);
                System.out.println("DEBUG 최종: " + content.getTitle() + "의 세팅된 진도율: " + content.getProgressPercent());
            }
        }
        return contentList;

    }

    public Content view(Long id) {
        Content content = null;
        Optional<Content> optionalContent = contentRepository.findById(id);
        if (optionalContent.isPresent()){
            content = optionalContent.get();
        }

        return content;
    }

    public void chugaProc(ContentDTO contentDTO){
        Category category = categoryRepository.findById(contentDTO.getCategoryId())
                .orElseThrow(()-> new IllegalArgumentException("해당 강의가 없습니다."));

        // 1. 비디오 파일 업로드 (Cloudinary)
        if (contentDTO.getVideoFile() != null && !contentDTO.getVideoFile().isEmpty()) {
            String videoUrl = uploadToCloudinary(contentDTO.getVideoFile(), "video");
            contentDTO.setFileName(videoUrl); // 이제 fileName엔 로컬 파일명이 아니라 Cloudinary URL이 통째로 저장됨
        }

        // 2. 썸네일 파일 업로드 (Cloudinary)
        if (contentDTO.getThumbFile() != null && !contentDTO.getThumbFile().isEmpty()) {
            String thumbUrl = uploadToCloudinary(contentDTO.getThumbFile(), "image");
            contentDTO.setThumbFileName(thumbUrl);
        }

        // 3. 첨부 파일 업로드 (Cloudinary, raw 타입)
        if (contentDTO.getAttachFile() != null && !contentDTO.getAttachFile().isEmpty()){
            String attachUrl = uploadToCloudinary(contentDTO.getAttachFile(), "raw");
            contentDTO.setAttachFileName(attachUrl);
        }

        Content content = createEntity(contentDTO, category);
        contentRepository.save(content);
    }

    @Transactional
    public void sujungProc(ContentDTO contentDTO) {
        Content content = contentRepository.findById(contentDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 없습니다."));

        content.setTitle(contentDTO.getTitle());
        content.setSequence(contentDTO.getSequence());
        content.setVideoUrl(contentDTO.getVideoUrl());

        // 그동안 누락되어 저장 안 되던 필드들
        content.setStage(contentDTO.getStage());
        content.setDescription(contentDTO.getDescription());
        content.setKeywords(contentDTO.getKeywords());
        content.setStatus(contentDTO.getStatus());
        content.setPublishAt(contentDTO.getPublishAt());
        content.setFree(contentDTO.isFree());

        // 영상 파일 (Cloudinary)
        if (contentDTO.getVideoFile() != null && !contentDTO.getVideoFile().isEmpty()) {
            String videoUrl = uploadToCloudinary(contentDTO.getVideoFile(), "video");
            content.setFileName(videoUrl);
            content.setFileOrigin(contentDTO.getVideoFile().getOriginalFilename());
        }

        // 썸네일 파일 (Cloudinary)
        if (contentDTO.getThumbFile() != null && !contentDTO.getThumbFile().isEmpty()) {
            String thumbUrl = uploadToCloudinary(contentDTO.getThumbFile(), "image");
            content.setThumbFileName(thumbUrl);
        }

        // 첨부 파일: 새 파일이 들어오면 교체, 삭제 체크면 비우고, 둘 다 아니면 기존 유지
        if (contentDTO.getAttachFile() != null && !contentDTO.getAttachFile().isEmpty()) {
            String attachUrl = uploadToCloudinary(contentDTO.getAttachFile(), "raw");
            content.setAttachFileName(attachUrl);
            content.setAttachFileOrigin(contentDTO.getAttachFile().getOriginalFilename());
        } else if (contentDTO.isDeleteAttach()) {
            content.setAttachFileName(null);
            content.setAttachFileOrigin(null);
        }
    }

    // 공통 업로드 메서드 (로컬 저장 대신 Cloudinary 업로드, secure_url 리턴)
    private String uploadToCloudinary(MultipartFile file, String resourceType) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", resourceType)
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cloudinary 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    public void sakjeProc(ContentDTO contentDTO) {
        Category category = categoryRepository.findById(contentDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 없습니다."));

        Content content = createEntity(contentDTO, category);
        contentRepository.delete(content);
    }

    public Content createEntity(ContentDTO contentDTO, Category category) {
        Content content = new Content();
        content.setId(contentDTO.getId());
        content.setTitle(contentDTO.getTitle());
        content.setVideoUrl(contentDTO.getVideoUrl());
        content.setSequence(contentDTO.getSequence());
        content.setCategory(category);

        // 1. 비디오 URL 세팅 (Cloudinary secure_url)
        content.setFileName(contentDTO.getFileName());

        // 2. 썸네일 URL 세팅
        content.setThumbFileName(contentDTO.getThumbFileName());

        // 3. 첨부파일 URL 세팅
        content.setAttachFileName(contentDTO.getAttachFileName());

        content.setStage(contentDTO.getStage());
        content.setDescription(contentDTO.getDescription());
        content.setKeywords(contentDTO.getKeywords());
        content.setStatus(contentDTO.getStatus());
        content.setPublishAt(contentDTO.getPublishAt());
        content.setFree(contentDTO.isFree());

        // 영상 원본 파일명
        if (contentDTO.getVideoFile() != null && !contentDTO.getVideoFile().isEmpty()) {
            content.setFileOrigin(contentDTO.getVideoFile().getOriginalFilename());
        }

        // 첨부파일 원본 파일명 (영상용 fileOrigin과 분리)
        if (contentDTO.getAttachFile() != null && !contentDTO.getAttachFile().isEmpty()){
            content.setAttachFileOrigin(contentDTO.getAttachFile().getOriginalFilename());
        }
        return content;
    }

    @Transactional
    public void completeContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(()->new IllegalArgumentException("차시 없음"));

        Progress progress = new Progress();
        progress.setContent(content);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }
    // 강의 단건 조회 메서드 (예시)
    public Content getContent(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의가 없습니다. id=" + id));
    }

    // 전체 강의 목록 조회 메서드 (예시)
    public List<Content> getAllContentList() {
        return contentRepository.findAll();
    }

    public Page<Content> getAllContentList(Pageable pageable) {
        return contentRepository.findAll(pageable);
    }

    // 결제 여부 확인 로직
    public boolean hasAccess(SiteUser user, String categoryTitle) {
        // OrderPay 테이블에서 해당 유저와 카테고리 이름으로 결제 내역 조회
        return orderPayRepository.existsBySiteUserAndCategory_Title(user, categoryTitle);
    }

    @Transactional
    public void saveUserProgress(Long contentId, String username, double percent, double time) {
        SiteUser user = this.siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        Content content = this.contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("콘텐츠 없음"));

        // 1. 기존 데이터 조회
        Progress progress = this.progressRepository.findBySiteUserAndContent(user, content)
                .orElse(null);

        // 2. 데이터가 없으면 새로 생성
        if (progress == null) {
            progress = new Progress();
            progress.setSiteUser(user);
            progress.setContent(content);
        } else {
            // 🌟 중요: 이미 100% 완료된 강의라면 다시 낮아지지 않도록 방어
            if (progress.isCompleted()) {
                return; // 이미 완료된 기록이 있으면 업데이트하지 않고 종료
            }
        }

        // 3. 값 업데이트
        progress.setPercentage(percent);
        progress.setLastWatchedTime(time);
        progress.setUpdatedAt(LocalDateTime.now()); // "마지막으로 본 강의" 계산용

        // 4. 완료 처리
        if (percent >= 100) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        this.progressRepository.save(progress);
    }

    // ContentService.java
    public double getAverageProgress(SiteUser user) {
        if (user == null) return 0;
        List<Progress> progressList = progressRepository.findBySiteUser(user);
        if (progressList == null || progressList.isEmpty()) return 0;

        double total = 0;
        for (Progress p : progressList) {
            total += p.getPercentage();
        }
        return total / progressList.size();
    }

    // 완료한 강의(차시) 개수 - "완료 강의" 통계용
    public int getCompletedCount(SiteUser user) {
        if (user == null) return 0;
        List<Progress> progressList = progressRepository.findBySiteUser(user);
        if (progressList == null) return 0;

        int count = 0;
        for (Progress p : progressList) {
            if (p.isCompleted()) count++;
        }
        return count;
    }

    // 가장 최근에 시청한 Progress - "마지막으로 본 강의" 박스용
    public Progress getLastWatchedProgress(SiteUser user) {
        if (user == null) return null;
        return progressRepository.findTopBySiteUserOrderByUpdatedAtDesc(user).orElse(null);
    }

    // 마이페이지 "학습 현황" 탭 - 최근 시청순 전체 목록 (완료/진행중 무관, 페이지네이션)
    public Page<Progress> getMyProgress(SiteUser user, Pageable pageable) {
        return progressRepository.findBySiteUserOrderByUpdatedAtDesc(user, pageable);
    }

    // 강좌별 진도율 목록 (라인 차트용)
    public List<Map<String, Object>> getCourseProgressList(SiteUser user) {
        List<OrderPay> paidList = orderPayRepository.findBySiteUser(user);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (OrderPay op : paidList) {
            if (op.getCategory() == null) continue;

            // 해당 강좌의 전체 콘텐츠 목록
            List<Content> contentList = contentRepository
                    .findByCategoryIdOrderBySequenceAsc(op.getCategory().getId());
            if (contentList.isEmpty()) continue;

            // 진도율 평균 계산
            double total = 0;
            for (Content c : contentList) {
                Optional<Progress> p = progressRepository.findBySiteUserAndContent(user, c);
                total += p.map(Progress::getPercentage).orElse(0.0);
            }
            double avg = total / contentList.size();

            Map<String, Object> item = new java.util.HashMap<>();
            item.put("title", op.getCategory().getTitle());
            item.put("percent", Math.round(avg));
            result.add(item);
        }
        return result;
    }

    // ── 💡 엔티티 설계(stage)에 맞추어 최종 보완된 수정 로직 ──
    @Transactional
    public void update(Long id, String title, String step, Integer lectureCount, String status) {
        // 1. ID로 수정할 데이터 조회
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠가 존재하지 않습니다. id=" + id));

        // 2. 제목(title)과 상태(status) 업데이트
        content.setTitle(title);
        content.setStatus(status);

        // 3. 화면의 글자(step)를 엔티티의 숫자(stage)로 변환하여 매핑
        int stageValue = 1; // 기본값 (음절)
        if ("단어".equals(step)) {
            stageValue = 2;
        } else if ("문장".equals(step)) {
            stageValue = 3;
        } else if ("일상회화".equals(step)) {
            stageValue = 4;
        }
        content.setStage(stageValue);

        // 4. ⚠️ 엔티티에 lectureCount(강의수) 필드가 없으므로,
        // 필요하다면 sequence 필드 등에 임시 저장하거나 혹은 주석 처리하여 에러를 방지합니다.
        if (lectureCount != null) {
            content.setSequence(lectureCount);
        }

        // @Transactional에 의해 메서드 종료 시 DB에 자동 반영됩니다.
    }

    @Transactional
    public void delete(Long id){

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("콘텐츠가 없습니다."));

        contentRepository.delete(content);
    }

    public Page<SiteUser> getAllUsers(Pageable pageable) {
        return siteUserRepository.findAll(pageable);
    }



}