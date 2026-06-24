package com.meta12.SS8911.controller;

import com.meta12.SS8911.Dto.SiteUserDTO;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.SiteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SiteUserController {
    private final SiteUserService siteUserService;

    @GetMapping("/siteUser/chuga") // 사용자가 /siteUser/chuga로 접속하면
    public String chugaForm(SiteUserDTO siteUserDTO) {
        return "siteUser/chuga"; // chuga.html 파일을 보여줌
    }

    @PostMapping("/siteUser/chugaProc")
    public String chugaProc(@Valid SiteUserDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "siteUser/chuga";
        if (!dto.getPassword().equals(dto.getPasswordChk())) {
            bindingResult.rejectValue("passwordChk", "error", "비밀번호가 일치하지 않습니다.");
            return "siteUser/chuga";
        }
        siteUserService.chugaProc(dto);
        return "redirect:/siteUser/login";
    }

    @GetMapping("/siteUser/profile/{username}")
    public String profile(@PathVariable("username") String username, Model model) {
        SiteUser user = siteUserService.getUserByUsername(username);
        model.addAttribute("siteUser", user);
        return "siteUser/view"; // 유저 상세 페이지로 이동
    }

    @GetMapping("/siteUser/login")
    public String login() {
        return "siteUser/login"; // 나중에 만들 로그인 HTML 파일명
    }

    @GetMapping("/siteUser/mypage")
    public String mypage(Model model, Principal principal) {
        SiteUser user = siteUserService.getUserByUsername(principal.getName());
        model.addAttribute("siteUser", user);
        return "siteUser/mypage";
    }
}