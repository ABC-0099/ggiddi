package com.meta12.SS8911.service;


import com.meta12.SS8911.dto.ContentDTO;
import com.meta12.SS8911.entity.*;
import com.meta12.SS8911.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    private final String uploadPath = "C:/meta12/masil/videos/";
    private final String thumbPath = "C:/meta12/masil/thumbnails/";
    private final ProgressRepository progressRepository;
    private final OrderPayRepository orderPayRepository;
    private final SiteUserRepository siteUserRepository;

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

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        File thumbDir = new File(thumbPath);
        if (!thumbDir.exists()) thumbDir.mkdirs();

        // 1. 비디오 파일 저장
        if (contentDTO.getVideoFile() != null && !contentDTO.getVideoFile().isEmpty()) {
            String originalFilename = contentDTO.getVideoFile().getOriginalFilename();
            String saveFileName = System.currentTimeMillis() + "_VIDEO_" + originalFilename;
            saveFile(contentDTO.getVideoFile(), uploadPath, saveFileName); // ← uploadPath
            contentDTO.setFileName(saveFileName);
        }

        // 2. 썸네일 파일 저장
        if (contentDTO.getThumbFile() != null && !contentDTO.getThumbFile().isEmpty()) {
            String originalFilename = contentDTO.getThumbFile().getOriginalFilename();
            String saveFileName = System.currentTimeMillis() + "_THUMB_" + originalFilename;
            saveFile(contentDTO.getThumbFile(), thumbPath, saveFileName); // ← thumbPath
            contentDTO.setThumbFileName(saveFileName);
        }

        // 3. 첨부 파일 저장
        if (contentDTO.getAttachFile() != null && !contentDTO.getAttachFile().isEmpty()){
            String originalFilename = contentDTO.getAttachFile().getOriginalFilename();
            String saveFileName = System.currentTimeMillis() + "_FILE_" + originalFilename;
            saveFile(contentDTO.getAttachFile(), uploadPath, saveFileName); // ← uploadPath
            contentDTO.setAttachFileName(saveFileName);
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

        // 영상 파일
        if (contentDTO.getVideoFile() != null && !contentDTO.getVideoFile().isEmpty()) {
            String saveFileName = System.currentTimeMillis() + "_VIDEO_" + contentDTO.getVideoFile().getOriginalFilename();
            saveFile(contentDTO.getVideoFile(), uploadPath, saveFileName); // ← uploadPath
            content.setFileName(saveFileName);
        }

        // 썸네일 파일
        if (contentDTO.getThumbFile() != null && !contentDTO.getThumbFile().isEmpty()) {
            String saveFileName = System.currentTimeMillis() + "_THUMB_" + contentDTO.getThumbFile().getOriginalFilename();
            saveFile(contentDTO.getThumbFile(), thumbPath, saveFileName); // ← thumbPath
            content.setThumbFileName(saveFileName);
        }

        // 첨부 파일
        if (contentDTO.getAttachFile() != null && !contentDTO.getAttachFile().isEmpty()) {
            String saveFileName = System.currentTimeMillis() + "_FILE_" + contentDTO.getAttachFile().getOriginalFilename();
            saveFile(contentDTO.getAttachFile(), uploadPath, saveFileName); // ← uploadPath
            content.setAttachFileName(saveFileName);
            content.setFileOrigin(contentDTO.getAttachFile().getOriginalFilename());
        }
    }

    // 공통 저장 메서드 (path 파라미터 추가)
    private void saveFile(MultipartFile file, String path, String saveName) {
        try {
            file.transferTo(new File(path + saveName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 파일 저장을 위한 공통 메서드 추출
    private void saveFile(MultipartFile file, String saveName) {
        try {
            file.transferTo(new File(uploadPath + saveName));

        } catch (IOException e) {
            e.printStackTrace();
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

        // 1. 비디오 파일명 세팅
        content.setFileName(contentDTO.getFileName());

        // 2. 썸네일 파일명 세팅 (이 부분이 누락되어 있었습니다!)
        content.setThumbFileName(contentDTO.getThumbFileName());

        // 3. 첨부파일 파일명 세팅 (이 부분도 함께 추가해야 안전합니다)
        content.setAttachFileName(contentDTO.getAttachFileName());

        content.setStage(contentDTO.getStage());
        content.setDescription(contentDTO.getDescription());
        content.setKeywords(contentDTO.getKeywords());
        content.setStatus(contentDTO.getStatus());
        content.setPublishAt(contentDTO.getPublishAt());
        content.setFree(contentDTO.isFree());


        if (contentDTO.getAttachFile() != null && !contentDTO.getAttachFile().isEmpty()){
            content.setFileOrigin(contentDTO.getAttachFile().getOriginalFilename());
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

        // 4. 완료 처리
        if (percent >= 100) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        this.progressRepository.save(progress);
    }

    // ContentService.java
    public double getAverageProgress(SiteUser user) {
        List<Progress> progressList = progressRepository.findBySiteUser(user);
        if (progressList == null || progressList.isEmpty()) return 0;

        double total = 0;
        for (Progress p : progressList) {
            total += p.getPercentage();
        }
        return total / progressList.size();
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

}
