package com.meta12.SS8911.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {

    @GetMapping("/game")
    public String questBoard() {
        return "game/list";
    }
}