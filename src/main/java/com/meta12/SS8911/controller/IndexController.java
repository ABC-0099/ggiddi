package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.ContentRepository;
import com.meta12.SS8911.repository.ProgressRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.service.AttendanceService;
import com.meta12.SS8911.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final CategoryService categoryService;
    private final SiteUserRepository siteUserRepository;
    private final ProgressRepository progressRepository;
    private final ContentRepository contentRepository;
    private final AttendanceService attendanceService; // 이 필드가 있어야 합니다.

    @GetMapping("/")
    public String list(Principal principal, Model model) {
        SiteUser user = null;
        if (principal != null) {
            user = siteUserRepository.findByUsername(principal.getName()).orElse(null);
        }

        if (user != null) {
            Content continueContent = categoryService.getContinueContent(user);
            model.addAttribute("continueContent", continueContent);

            long completedCount = progressRepository.countBySiteUserAndCompletedTrue(user);
            long totalPublished = contentRepository.countByStatus("PUBLISHED");
            int progressPercent = (totalPublished == 0) ? 0 : (int) Math.round(completedCount * 100.0 / totalPublished);

            model.addAttribute("completedCount", completedCount);

            // ★ 수정: 저장된 필드 대신 서비스의 실시간 계산 메서드 사용
            int streak = attendanceService.getCurrentStreak(user.getUsername());
            model.addAttribute("streakDays", streak);

            model.addAttribute("progressPercent", progressPercent);
        }

        return "main/mainpage";
    }
}