package com.meta12.SS8911.service;

import com.meta12.SS8911.controller.Category;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;

    // 카테고리에 따른 동적 조회
    public List<Community> getCommunityPosts(Category category) {
        if (category == null || category == Category.ALL) {
            return communityRepository.findAllByOrderByCreatedDateDesc(); // 전체 조회
        }
        return communityRepository.findByCategoryOrderByCreatedDateDesc(category); // 특정 카테고리 조회
    }
}
