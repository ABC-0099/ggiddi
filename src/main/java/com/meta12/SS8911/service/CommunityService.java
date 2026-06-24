package com.meta12.SS8911.service;

import com.meta12.SS8911.Dto.CommunityDTO;
import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;

    // 기존 코드
    public List<Community> getCommunityPosts(Category category) {
        if (category == null || category == Category.ALL) {
            return communityRepository.findAllByOrderByCreatedDateDesc();
        }
        return communityRepository.findByCategoryOrderByCreatedDateDesc(category);
    }

    public void create(CommunityDTO dto, SiteUser author) {
        Community community = new Community();
        community.setTitle(dto.getTitle());
        community.setContent(dto.getContent());
        community.setCategory(dto.getCategory());
        community.setAuthor(author);
        community.setCreatedDate(LocalDateTime.now());
        communityRepository.save(community);
    }

    public Community getPost(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
    }

    public List<Community> getPostsByAuthor(SiteUser author) {
        return communityRepository.findByAuthorOrderByCreatedDateDesc(author);
    }
}