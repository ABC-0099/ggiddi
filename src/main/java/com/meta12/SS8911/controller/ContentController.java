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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
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
    public String view(@PathVariable("id") Long id, Model model, Principal principal) {
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

        // 3. 권한 체크
        if (currentUser != null) {
            if (currentUser.getRole() == Role.ADMIN) {
                hasAccess = true;
            } else {
                isPaid = orderPayRepository.existsBySiteUserAndCategory(currentUser, category);
                hasAccess = isPaid;
            }
        }
        System.out.println("[DEBUG-4] hasAccess = " + hasAccess); // 추가

        // 4. 권한 없을 때
        if (!hasAccess) {
            model.addAttribute("alertMsg", "이 강의는 수강 신청을 하셔야 보실 수 있어요 어르신!");
            model.addAttribute("redirectUrl", "/category/view/" + category.getId());
            model.addAttribute("contentList", contentService.list(category.getId(), currentUser));
            model.addAttribute("category", category);
            model.addAttribute("isPaid", false);
            return "content/management";
        }

        // 5. 권한 있을 때
        // 5. 권한 있을 때 부분에 추가
        // 5. 권한 있을 때 부분에 추가
        Progress progress = progressRepository.findBySiteUserAndContent(currentUser, content).orElse(null);
        model.addAttribute("savedTime", (progress != null) ? progress.getLastWatchedTime() : 0);
        model.addAttribute("content", content);
        model.addAttribute("contentList", contentService.list(category.getId(), currentUser));
        model.addAttribute("isPaid", true);

        // ── 영상 완료 후 임베드 퀴즈 ──
        Quiz quiz = quizService.getQuizEntityForContent(id);
        if (quiz != null) {
            model.addAttribute("quizId", quiz.getId());
            model.addAttribute("quizTitle", quiz.getTitle());
            boolean quizUnlocked = quizService.isUnlocked(quiz, principal.getName());
            model.addAttribute("quizUnlocked", quizUnlocked);
        } else {
            model.addAttribute("quizId", null);
        }

        return "content/view";
    }

    // [수정/삭제/기타 Proc 메서드들은 기존과 동일합니다]
    // ... (이후 chuga, sujung, sakje 관련 메서드 유지)

    @GetMapping("/content/sujung/{id}")
    public String sujung(Model model, @PathVariable("id") Long id) {
        Content content = contentService.view(id);
        if (content == null) return "redirect:/";
        model.addAttribute("content", content);
        return "content/sujung";
    }

    @GetMapping("/content/sakje/{id}")
    public String sakje(Model model, @PathVariable("id") Long id) {
        Content content = contentService.view(id);
        if (content == null) return "redirect:/";
        model.addAttribute("content", content);
        return "content/sakje";
    }

//    @PostMapping("/content/chugaProc")
//    public String chugaProc(ContentDTO contentDTO) {
//        contentService.chugaProc(contentDTO);
//        return "redirect:/category/view/" + contentDTO.getCategoryId();
//    }

    @PostMapping("/content/chugaProc")
    public String chugaProc(ContentDTO contentDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 로그 찍어서 원인 확인
            bindingResult.getAllErrors().forEach(e -> System.out.println(e));
            return "redirect:/content/chuga/" + contentDTO.getCategoryId();
        }
        contentService.chugaProc(contentDTO);
        return "redirect:/category/view/" + contentDTO.getCategoryId();
    }

    @PostMapping("/content/sujungProc")
    public String sujungProc(ContentDTO contentDTO) {
        contentService.sujungProc(contentDTO);
        return "redirect:/category/view/" + contentDTO.getCategoryId();
    }

    @PostMapping("/content/sakjeProc")
    public String sakjeProc(ContentDTO contentDTO) {
        contentService.sakjeProc(contentDTO);
        return "redirect:/category/view/" + contentDTO.getCategoryId();
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
    // downloadFile()은 강제 다운로드용(Content-Disposition: attachment)이라 <video> 태그에서 못 씀.
    @GetMapping("/content/stream/{id}")
    public ResponseEntity<Resource> streamFile(@PathVariable("id") Long id) throws MalformedURLException {
        Content content = contentService.view(id);
        if (content == null || content.getFileName() == null) {
            return ResponseEntity.notFound().build();
        }

        UrlResource resource = new UrlResource("file:C:/meta12/masil/videos/" + content.getFileName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @GetMapping("/content/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) throws MalformedURLException {
        Content content = contentService.view(id);
        UrlResource resource = new UrlResource("file:C:/meta12/masil/videos/" + content.getFileName());
        String encodedName = UriUtils.encode(content.getFileOrigin(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
                .body(resource);
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