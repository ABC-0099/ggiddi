package com.meta12.SS8911.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class Index {

    @GetMapping("/")
    public String list(){
        return "main/mainpage";
    }
}
