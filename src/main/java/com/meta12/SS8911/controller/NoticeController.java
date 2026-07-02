package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.NoticeDTO;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final SiteUserRepository siteUserRepository;

    @GetMapping("/main")
    public String noticeMain() {
        return "notice/main"; // notice 폴더 안의 main.html 파일을 연다는 뜻
    }

    // 1. 공지 리스트
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String category,
                       Model model) {
        Page<NoticeDTO> noticePage = noticeService.getNoticePage(page, category);
        model.addAttribute("list", noticePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", noticePage.getTotalPages());
        model.addAttribute("category", category);
        return "notice/list";
    }

    // 2. 상세보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNotice(id));
        return "notice/view";
    }

    // 3. 작성 페이지 이동
    @GetMapping("/chuga")
    public String chuga(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null || !"admin888".equals(userDetails.getUsername())) {
            return "redirect:/notice/list?error=unauthorized";
        }
        model.addAttribute("noticeDTO", new NoticeDTO());
        return "notice/chuga";
    }

    // 4. 새 글 저장 (중요: DB에서 유저를 찾아 서비스에 전달)
    @PostMapping("/save")
    public String save(@ModelAttribute("noticeDTO") NoticeDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !"admin888".equals(userDetails.getUsername())) {
            return "redirect:/notice/list?error=unauthorized";
        }

        // 시큐리티 유저 정보를 이용해 DB의 SiteUser 엔티티를 가져옴
        SiteUser loginUser = siteUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        dto.setAuthor(loginUser); // 작성자 주입

        try {
            noticeService.createNotice(dto);
        } catch (RuntimeException e) {
            return "redirect:/notice/list?error=unauthorized";
        }
        return "redirect:/notice/list";
    }

    // 5. 수정 페이지 이동
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !"admin888".equals(userDetails.getUsername())) {
            return "redirect:/notice/list?error=unauthorized";
        }
        model.addAttribute("notice", noticeService.getNotice(id));
        return "notice/sujung";
    }

    // 6. 실제 수정 처리
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("notice") NoticeDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !"admin888".equals(userDetails.getUsername())) {
            return "redirect:/notice/list?error=unauthorized";
        }

        SiteUser loginUser = siteUserRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        dto.setId(id);
        dto.setAuthor(loginUser); // 권한 체크를 위해 작성자 정보 필수

        try {
            noticeService.updateNotice(id, dto);
        } catch (RuntimeException e) {
            return "redirect:/notice/list?error=unauthorized";
        }
        return "redirect:/notice/view/" + id;
    }

    // 7. 삭제 확인 페이지
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !"admin888".equals(userDetails.getUsername())) {
            return "redirect:/notice/list?error=unauthorized";
        }
        model.addAttribute("notice", noticeService.getNotice(id));
        return "notice/sakje";
    }

    // 8. 실제 삭제 실행
    @PostMapping("/delete/{id}")
    public String deleteReal(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || !"admin888".equals(userDetails.getUsername())) {
            return "redirect:/notice/list?error=unauthorized";
        }
        try {
            noticeService.deleteNotice(id);
        } catch (RuntimeException e) {
            return "redirect:/notice/list?error=unauthorized";
        }
        return "redirect:/notice/list";
    }
}