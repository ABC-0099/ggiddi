package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.QnaDTO;
import com.meta12.SS8911.entity.Qna;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.service.QnaService;
import com.meta12.SS8911.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {


    private final SiteUserService siteUserService;

    @GetMapping
    public String main() {
        return "event/event";
    }


}