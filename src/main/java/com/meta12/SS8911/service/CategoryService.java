package com.meta12.SS8911.service;


import com.meta12.SS8911.dto.CategoryDTO;
import com.meta12.SS8911.entity.*;
import com.meta12.SS8911.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ContentRepository contentRepository;
    private final ProgressRepository progressRepository;
    private final OrderPayRepository orderPayRepository;

    private final String uploadPath = "C:/meta12/masil/category_images/";


    public List<Category> findAll() {
        return this.categoryRepository.findAll();
    }

    public List<Content> list(Long categoryId, SiteUser user) {
        List<Content> contentList = contentRepository.findByCategoryIdOrderBySequenceAsc(categoryId);
        for (Content content : contentList) {
            if (user != null) {
                Optional<Progress> progress = progressRepository.findBySiteUserAndContent(user, content);
                if (progress.isPresent()) {
                    // double 타입인 percentage를 int로 올바르게 강제 형변환(Casting)
                    int val = (int) Math.round(progress.get().getPercentage());
                    content.setProgressPercent(val);
                } else {
                    content.setProgressPercent(0);
                }
            } else {
                content.setProgressPercent(0);
            }
        }
        return contentList;
    }

    public Category view(Long id){
        Category category = null;
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if(optionalCategory.isPresent()){
            category = optionalCategory.get();
        }
        return category;
    }

    public void chugaProc(CategoryDTO categoryDTO){

        if (categoryDTO.getAttachFile() != null && !categoryDTO.getAttachFile().isEmpty()){
            String originalFilename = categoryDTO.getAttachFile().getOriginalFilename();
            String saveFileName = "FILE_" + System.currentTimeMillis() + "_" + originalFilename;

            try{
                File saveFile = new File(uploadPath + saveFileName);
                categoryDTO.getAttachFile().transferTo(saveFile);
                categoryDTO.setFileName(saveFileName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        Category category = createEntity(categoryDTO);
        categoryRepository.save(category);


    }

    public void sujungProc(CategoryDTO categoryDTO){
        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. id=" + categoryDTO.getId()));

        category.setTitle(categoryDTO.getTitle());
        category.setInstructor(categoryDTO.getInstructor());
        category.setDescription(categoryDTO.getDescription());
        category.setFileName(categoryDTO.getFileName());

        if (categoryDTO.getAttachFile() != null && !categoryDTO.getAttachFile().isEmpty()) {
            category.setFileOrigin(categoryDTO.getAttachFile().getOriginalFilename());
        }

        categoryRepository.save(category);
    }

    @Transactional
    public void sakjeProc(CategoryDTO categoryDTO) {
        orderPayRepository.deleteByCategoryId(categoryDTO.getId());
        Category category = createEntity(categoryDTO);
        categoryRepository.delete(category);
    }

    public Category createEntity(CategoryDTO categoryDTO){
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setTitle(categoryDTO.getTitle());
        category.setInstructor(categoryDTO.getInstructor());
        category.setDescription(categoryDTO.getDescription());
        category.setFileName(categoryDTO.getFileName());

        if (categoryDTO.getAttachFile() == null || categoryDTO.getAttachFile().isEmpty()){

        }else{
            category.setFileOrigin(categoryDTO.getAttachFile().getOriginalFilename());
        }
        return category;
    }

    @Transactional
    public void delete(Long id) {
        orderPayRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteMultiple(List<Long> ids) {
        for (Long id : ids) {
            if (categoryRepository.existsById(id)) {
                // order_pay 먼저 삭제
                orderPayRepository.deleteByCategoryId(id);
                categoryRepository.deleteById(id);
            }
        }
    }

    // CategoryService.java에 추가
    public int getCategoryProgress(Category category, SiteUser user) {
        if (user == null) return 0;

        // 1. 해당 카테고리의 모든 콘텐츠 가져오기
        List<Content> contentList = contentRepository.findByCategoryIdOrderBySequenceAsc(category.getId());
        if (contentList.isEmpty()) return 0;

        // 2. 콘텐츠별 진도율 합산
        double totalPercent = 0;
        for (Content content : contentList) {
            // ContentService의 로직을 활용하거나 직접 ProgressRepository 조회
            totalPercent += progressRepository.findBySiteUserAndContent(user, content)
                    .map(Progress::getPercentage)
                    .orElse(0.0);
        }

        // 3. 평균 계산 후 반올림
        return (int) Math.round(totalPercent / contentList.size());
    }

}
