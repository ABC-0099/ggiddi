package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.ContentDTO;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.Progress;
import com.meta12.SS8911.entity.Quiz;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.OrderPayRepository;
import com.meta12.SS8911.repository.ProgressRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.service.CategoryService;
import com.meta12.SS8911.service.ContentService;
import com.meta12.SS8911.service.QuizService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final CategoryService categoryService;
    private final QuizService quizService;
    private final SiteUserService siteUserService;

    private final OrderPayRepository orderPayRepository;
    private final SiteUserRepository siteUserRepository;
    private final ProgressRepository progressRepository;




    @GetMapping("/content/chuga/{categoryId}")
    public String chuga(Model model, @PathVariable("categoryId") Long categoryId) {
        Category category = categoryService.view(categoryId);
//        if (category == null) return "redirect:/category/list";
        model.addAttribute("category", category);

        // 폼(th:object="${lectureForm}")이 바인딩할 빈 DTO. categoryId는 미리 채워서 hidden 필드로 내려보냄
        ContentDTO lectureForm = new ContentDTO();
        lectureForm.setCategoryId(categoryId);
        model.addAttribute("lectureForm", lectureForm);

        return "content/chuga";
    }

    @GetMapping("/content/view/{id}")
    public String view(@PathVariable("id") Long id, Model model, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // 1. 유저 정보 먼저 가져오기
        System.out.println("[DEBUG-1] principal = " + (principal == null ? "null" : principal.getName())); // 추가

        SiteUser currentUser = null;
        if (principal != null) {
            currentUser = siteUserRepository.findByUsername(principal.getName()).orElse(null);
        }
        System.out.println("[DEBUG-2] currentUser = " + currentUser); // 추가
        if (currentUser != null) {
            System.out.println("[DEBUG-3] role = " + currentUser.getRole()); // 추가
        }

        // 2. 단건 조회는 contentService.view(id)를 사용 (List가 아님!)
        Content content = contentService.view(id);
        if (content == null) return "redirect:/";

        Category category = content.getCategory();
        boolean hasAccess = false;
        boolean isPaid = false;

        // 3. 권한 체크 (무료 콘텐츠는 결제 여부와 상관없이 열람 가능)
        if (content.isFree()) {
            hasAccess = true;
        } else if (currentUser != null) {
            if (currentUser.getRole() == Role.ADMIN) {
                hasAccess = true;
            } else {
                isPaid = orderPayRepository.existsBySiteUserAndCategory(currentUser, category);
                hasAccess = isPaid;
            }
        }
        System.out.println("[DEBUG-4] hasAccess = " + hasAccess); // 추가

        // 4. 권한 없을 때 (존재하지 않는 템플릿으로 forward하면 500 에러가 나므로,
        //    강의 목록(카테고리 화면)으로 redirect하고 알림 메시지만 전달)
        if (!hasAccess) {
            redirectAttributes.addFlashAttribute("alertMsg", "이 강의는 수강 신청을 하셔야 보실 수 있어요 어르신!");
            return "redirect:/category/view/" + category.getId();
        }

        // 5. 권한 있을 때
        Progress progress = progressRepository.findBySiteUserAndContent(currentUser, content).orElse(null);
        model.addAttribute("savedTime", (progress != null) ? progress.getLastWatchedTime() : 0);
        model.addAttribute("content", content);
        model.addAttribute("contentList", contentService.list(category.getId(), currentUser));
        model.addAttribute("isPaid", true);

        // ── 영상 완료 후 임베드 퀴즈 ──
        Quiz quiz = quizService.getQuizEntityForContent(id);
        if (quiz != null && principal != null) {
            model.addAttribute("quizId", quiz.getId());
            model.addAttribute("quizTitle", quiz.getTitle());
            boolean quizUnlocked = quizService.isUnlocked(quiz, principal.getName());
            model.addAttribute("quizUnlocked", quizUnlocked);
        } else {
            model.addAttribute("quizId", null);
        }

        return "content/view";
    }

    @GetMapping("/content/sujung/{id}")
    public String sujung(Model model, @PathVariable("id") Long id) {
        Content content = contentService.view(id);
        if (content == null) return "redirect:/";

        ContentDTO lectureForm = new ContentDTO();
        lectureForm.setId(content.getId());
        lectureForm.setTitle(content.getTitle());
        lectureForm.setSequence(content.getSequence());
        lectureForm.setCategoryId(content.getCategory() != null ? content.getCategory().getId() : null);
        lectureForm.setStage(content.getStage());
        lectureForm.setDescription(content.getDescription());
        lectureForm.setKeywords(content.getKeywords());
        lectureForm.setStatus(content.getStatus());
        lectureForm.setPublishAt(content.getPublishAt());
        lectureForm.setFree(content.isFree());
        lectureForm.setFileName(content.getFileName());
        lectureForm.setThumbFileName(content.getThumbFileName());
        lectureForm.setAttachFileName(content.getAttachFileName());
        lectureForm.setAttachFileOrigin(content.getAttachFileOrigin());
        lectureForm.setVideoOriginalName(content.getFileOrigin());
        // fileName엔 이제 Cloudinary URL이 통째로 들어있으므로, 재생용 경로는 그대로 스트림 엔드포인트로 유지
        lectureForm.setVideoUrl(content.getFileName() != null ? "/content/stream/" + content.getId() : null);

        model.addAttribute("lectureForm", lectureForm);
        return "content/sujung";
    }

    @GetMapping("/content/sakje/{id}")
    public String sakje(Model model, @PathVariable("id") Long id) {
        Content content = contentService.view(id);
        if (content == null) return "redirect:/";
        model.addAttribute("content", content);
        return "content/sakje";
    }

    @PostMapping("/content/chugaProc")
    public String chugaProc(ContentDTO contentDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 로그 찍어서 원인 확인
            bindingResult.getAllErrors().forEach(e -> System.out.println(e));
            return "redirect:/content/chuga/" + contentDTO.getCategoryId();
        }
        contentService.chugaProc(contentDTO);
        return "redirect:/category/list";
    }

    @PostMapping("/content/sujungProc")
    public String sujungProc(ContentDTO contentDTO) {
        contentService.sujungProc(contentDTO);
        return "redirect:/category/list";
    }

    @PostMapping("/content/sakjeProc")
    public String sakjeProc(ContentDTO contentDTO) {
        contentService.sakjeProc(contentDTO);
        return "redirect:/category/list";
    }

    @PostMapping("/content/complete/{id}")
    @ResponseBody
    public String complete(@PathVariable("id") Long id,
                           @RequestBody(required = false) Map<String, Object> data, // 빈 데이터도 허용
                           Principal principal) {

        if (principal == null) return "not_logged_in";

        try {
            SiteUser siteUser = this.siteUserService.getUser(principal.getName());
            Content content = this.contentService.getContent(id);

            if (siteUser == null || content == null) return "error";

            if (!progressRepository.existsBySiteUserAndContent(siteUser, content)) {
                Progress progress = new Progress();
                progress.setSiteUser(siteUser);
                progress.setContent(content);
                progress.setCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                this.progressRepository.save(progress);
            }
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 플레이어에서 바로 재생(스트리밍)할 때 쓰는 엔드포인트.
    // fileName엔 이제 Cloudinary secure_url이 통째로 저장되어 있으므로,
    // 로컬 파일을 읽는 대신 그 URL로 302 redirect만 해주면 <video> 태그가 알아서 재생함.
    @GetMapping("/content/stream/{id}")
    public ResponseEntity<Void> streamFile(@PathVariable("id") Long id) {
        Content content = contentService.view(id);
        if (content == null || content.getFileName() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(content.getFileName()))
                .build();
    }

    // 강제 다운로드도 마찬가지로 Cloudinary URL로 redirect.
    // 파일명을 원본 이름으로 강제하고 싶으면 Cloudinary URL에 fl_attachment 플래그를 붙이는 방법도 있음.
    @GetMapping("/content/download/{id}")
    public ResponseEntity<Void> downloadFile(@PathVariable("id") Long id) {
        Content content = contentService.view(id);
        if (content == null || content.getFileName() == null) {
            return ResponseEntity.notFound().build();
        }
        // fl_attachment를 붙이면 Cloudinary가 Content-Disposition: attachment로 내려줌
        String downloadUrl = content.getFileName().replace("/upload/", "/upload/fl_attachment/");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(downloadUrl))
                .build();
    }

    @PostMapping("/content/progress/{id}")
    @ResponseBody
    public String updateProgress(@PathVariable("id") Long id,
                                 @RequestBody(required = false) Map<String, Object> data,
                                 Principal principal) {
        if (principal == null) return "not_logged_in";

        double percent = data != null && data.containsKey("percentage")
                ? Double.parseDouble(data.get("percentage").toString()) : 0.0;
        double time = data != null && data.containsKey("lastWatchedTime")
                ? Double.parseDouble(data.get("lastWatchedTime").toString()) : 0.0;

        // 서비스 호출하여 DB 업데이트 (이 메서드를 서비스에 만드셔야 합니다)
        this.contentService.saveUserProgress(id, principal.getName(), percent, time);

        return "ok";
    }


}