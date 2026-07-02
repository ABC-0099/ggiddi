package com.meta12.SS8911.controller;

import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.dto.CategoryDTO;
import com.meta12.SS8911.entity.Category;
import com.meta12.SS8911.entity.Content;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.ContentRepository;
import com.meta12.SS8911.repository.OrderPayRepository;
import com.meta12.SS8911.repository.ProgressRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.service.CategoryService;
import com.meta12.SS8911.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.io.File;
import java.security.Principal;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class CategoryController {
    private final CategoryService categoryService;
    private final ContentService contentService;
    private final SiteUserRepository siteUserRepository;
    //    private final TeacherRepository teacherRepository;
    private final OrderPayRepository orderPayRepository;
    private final ContentRepository contentRepository;
    private final ProgressRepository progressRepository;

    @GetMapping("/category/main")
    public String main() {
        return "category/main";
    }



    @GetMapping("/category/list")
    public String list(
            @AuthenticationPrincipal SiteUser user,
            Model model
    ) { 
        List<Category> categoryList = categoryService.findAll();

        Map<Long, List<Content>> contentMap = new LinkedHashMap<>();
        Map<Long, Boolean> paidMap = new HashMap<>();

        for (Category category : categoryList) {
            contentMap.put(category.getId(), contentService.list(category.getId(), user));

            boolean isPaid = false;
            if (user != null) {
                if (user.getRole() == Role.ADMIN) {
                    isPaid = true;
                } else {
                    isPaid = orderPayRepository.existsBySiteUserAndCategory(user, category);
                }
            }
            paidMap.put(category.getId(), isPaid);
        }

        model.addAttribute("list", categoryList);
        model.addAttribute("contentMap", contentMap);
        model.addAttribute("paidMap", paidMap);

        return "category/list";
    }

    @GetMapping("/category/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id,
            Principal principal
    ) {
        Category category = categoryService.view(id);
        if (category == null) {
            return "redirect:/";
        }

        // 1. 유저 정보 먼저 가져오기 (서비스에서 진도율 계산용)
        SiteUser currentUser = null;
        if (principal != null) {
            currentUser = siteUserRepository.findByUsername(principal.getName()).orElse(null);
        }

        // 2. 💡 수정: 유저 정보를 함께 넘겨서 진도율(isCompleted)이 계산된 리스트를 가져옵니다.
        List<Content> contentList = contentService.list(id, currentUser);

        // 3. 결제/권한 상태 확인 로직
        boolean isPaid = false;
        if (principal != null) {
            // 관리자/강사 권한 체크
            if (currentUser != null && (currentUser.getRole() == Role.ADMIN)) {
                isPaid = true;
            } else if (currentUser != null) {
                isPaid = orderPayRepository.existsBySiteUserAndCategory(currentUser, category);
            }
        }

        model.addAttribute("category", category);
        model.addAttribute("contentList", contentList);
        model.addAttribute("isPaid", isPaid);

        return "category/view";
    }

    @GetMapping("/category/chuga")
    public String chuga() {
        return "category/chuga";
    }

    @GetMapping("/category/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id
    ) {
        Category category = categoryService.view(id);
        if (category == null) {
            return "redirect:/";
        }

        model.addAttribute("category", category);
        return "category/sujung";
    }

    @GetMapping("/category/sakje")
    public String sakje(
            Model model,
            @RequestParam("ids") List<Long> ids
    ) {

        for (Long id : ids) {
            if (categoryService.view(id) == null) {
                return "redirect:/";
            }
        }

        categoryService.deleteMultiple(ids);

        return "redirect:/category/list";
    }

    // 🌟 [수정]: HTML이 준 파일(categoryImage)을 받아서 저장하고 파일명을 DTO에 세팅합니다.
    @PostMapping("/category/chugaProc")
    public String chugaProc(
            CategoryDTO categoryDTO,
            @RequestParam(value = "categoryImage", required = false) MultipartFile categoryImage
    ) {
        if (categoryImage != null && !categoryImage.isEmpty()) {
            // 아래 만들어두신 유틸 메서드를 활용해 파일명을 받아옵니다.
            String fileName = saveCategoryImage(categoryImage);
            if (fileName != null) {
                categoryDTO.setFileName(fileName);
                categoryDTO.setFileOrigin(categoryImage.getOriginalFilename());
            }
        }

        categoryService.chugaProc(categoryDTO);
        return "redirect:/category/list";
    }

    // 🌟 [수정]: 수정할 때도 새 파일이 들어오면 교체하고, 안 들어오면 기존 파일명을 유지시킵니다.
    @PostMapping("/category/sujungProc")
    public String sujungProc(
            CategoryDTO categoryDTO,
            @RequestParam(value = "categoryImage", required = false) MultipartFile categoryImage
    ) {
        Category category = categoryService.view(categoryDTO.getId());
        if (category == null) {
            return "redirect:/";
        }

        if (categoryImage != null && !categoryImage.isEmpty()) {
            // 새 이미지가 정상 첨부되었다면 업로드 진행
            String fileName = saveCategoryImage(categoryImage);
            if (fileName != null) {
                categoryDTO.setFileName(fileName);
                categoryDTO.setFileOrigin(categoryImage.getOriginalFilename());
            }
        } else {
            // 새 이미지가 안 들어왔다면 기존 DB에 있던 파일명을 지켜줍니다.
            categoryDTO.setFileName(category.getFileName());
            categoryDTO.setFileOrigin(category.getFileOrigin());
        }

        categoryService.sujungProc(categoryDTO);
        return "redirect:/category/view/" + categoryDTO.getId();
    }

    @PostMapping("/category/sakjeProc")
    public String sakjeProc(
            CategoryDTO categoryDTO
    ) {
        Category category = categoryService.view(categoryDTO.getId());
        if (category == null) {
            return "redirect:/";
        }
        categoryService.sakjeProc(categoryDTO);
        return "redirect:/category/list";
    }

    @GetMapping("/category/{id}/content/add")
    public String addContentForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("categoryId", id);
        return "content/chuga";
    }

    // 🌟 [수정]: 컨트롤러 흐름에 맞게 리턴 타입을 파일명(String)으로 변환하여 유연하게 보정했습니다.
    private String saveCategoryImage(MultipartFile categoryImage) {
        if (categoryImage != null && !categoryImage.isEmpty()) {
            try {
                String fileOrigin = categoryImage.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String extension = fileOrigin.substring(fileOrigin.lastIndexOf("."));
                String fileName = uuid + extension;

                String uploadDir = "C:/meta12/masil/category_images/";
                File saveFile = new File(uploadDir + fileName);

                if (!saveFile.getParentFile().exists()) {
                    saveFile.getParentFile().mkdirs();
                }

                categoryImage.transferTo(saveFile);

                // 성공하면 물리적으로 저장된 유니크한 파일 이름을 반환합니다.
                return fileName;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}