package com.meta12.SS8911.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiChatPageController {

    @GetMapping("/ai-chat")
    public String aiChatPage() {
        return "ai-chat";  // templates/ai-chat.html
    }
}