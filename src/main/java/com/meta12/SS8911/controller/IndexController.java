package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.SiteUserRepository;
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

    @GetMapping("/")
    public String list(
            Principal principal,
            Model model
    ) {
        SiteUser user = null;
        if (principal != null) {
            user = siteUserRepository.findByUsername(principal.getName()).orElse(null);
        }

        // 🌟 [추가]: 메인페이지 "학습 이어가기" 버튼이 갈 실제 목적지
        // (category/list의 "이어보기"와 동일한 로직 - 전체 커리큘럼 중 완료 안 한 첫 강의)
        if (user != null) {
            Content continueContent = categoryService.getContinueContent(user);
            model.addAttribute("continueContent", continueContent);
        }

        return "main/mainpage";
    }
}