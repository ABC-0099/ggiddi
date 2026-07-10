package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.MockExamAdminDTO;
import com.meta12.SS8911.repository.CategoryRepository;
import com.meta12.SS8911.service.MockExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/mock")
@RequiredArgsConstructor
public class AdminMockExamController {

    private final MockExamService mockExamService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "모의고사 관리");
        model.addAttribute("exams", mockExamService.getAllForAdmin());
        return "mock/admin/list";
    }

    @GetMapping("/chuga")
    public String chuga(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "mock/admin/chuga";
    }

    @GetMapping("/sujung/{id}")
    public String sujung(@PathVariable Long id, Model model) {
        model.addAttribute("exam", mockExamService.getForAdmin(id));
        model.addAttribute("categories", categoryRepository.findAll());
        return "mock/admin/sujung";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("exam", mockExamService.getForAdmin(id));
        return "mock/admin/view";
    }
}