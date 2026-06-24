package com.meta12.SS8911.controller;

import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.CommunityService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final SiteUserService siteUserService;
    private final CommunityService communityService;

    @GetMapping
    public String myPage(Principal principal, Model model) {
        SiteUser user = siteUserService.getUser(principal.getName());
        List<Community> myPosts = communityService.getPostsByAuthor(user);

        model.addAttribute("user", user);
        model.addAttribute("myPosts", myPosts);
        return "mypage";
    }
}