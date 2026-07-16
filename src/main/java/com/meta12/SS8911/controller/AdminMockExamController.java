package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.MockExamAdminDTO;
import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.repository.CategoryRepository;
import com.meta12.SS8911.service.MockExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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

        List<MockExamAdminDTO> allExams = mockExamService.getAllForAdmin();

        // 카테고리 id 순서(K-POP → K-DRAMA → 일상회화 → 심화)로 그룹핑
        List<Category> categories = categoryRepository.findAll();
        categories.sort(Comparator.comparing(Category::getId));

        LinkedHashMap<String, List<MockExamAdminDTO>> grouped = new LinkedHashMap<>();
        for (Category cat : categories) {
            List<MockExamAdminDTO> forCat = allExams.stream()
                    .filter(e -> cat.getTitle().equals(e.getCategoryName()))
                    .collect(Collectors.toList());
            if (!forCat.isEmpty()) {
                grouped.put(cat.getTitle(), forCat);
            }
        }

        model.addAttribute("groupedExams", grouped);
        return "mock/admin/list";
    }

    @GetMapping("/chuga")
    public String chuga(Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "모의고사 등록");
        model.addAttribute("categories", categoryRepository.findAll());
        return "mock/admin/chuga";
    }

    @GetMapping("/sujung/{id}")
    public String sujung(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "모의고사 수정");
        model.addAttribute("exam", mockExamService.getForAdmin(id));
        model.addAttribute("categories", categoryRepository.findAll());
        return "mock/admin/sujung";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "quiz");
        model.addAttribute("pageTitle", "모의고사 상세보기");
        model.addAttribute("exam", mockExamService.getForAdmin(id));
        return "mock/admin/view";
    }
}