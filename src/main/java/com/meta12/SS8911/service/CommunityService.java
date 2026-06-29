package com.meta12.SS8911.service;

import com.meta12.SS8911.Dto.CommunityDTO;
import com.meta12.SS8911.config.Category;
import com.meta12.SS8911.config.Role;
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

    public List<Community> getCommunityPosts(Category category, String sort) {
        boolean oldest = "oldest".equals(sort);
        if (category == null || category == Category.ALL) {
            return oldest ? communityRepository.findAllWithAuthorOldest()
                    : communityRepository.findAllWithAuthor();
        }
        return oldest ? communityRepository.findByCategoryWithAuthorOldest(category)
                : communityRepository.findByCategoryWithAuthor(category);
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

    // 수정
    public void update(Long id, CommunityDTO dto, SiteUser user) {
        Community community = getPost(id);
        checkPermission(community, user);
        community.setTitle(dto.getTitle());
        community.setContent(dto.getContent());
        community.setCategory(dto.getCategory());
        communityRepository.save(community);
    }

    // 삭제
    public void delete(Long id, SiteUser user) {
        Community community = getPost(id);
        checkPermission(community, user);
        communityRepository.delete(community);
    }

    // 작성자 또는 관리자 권한 체크
    private void checkPermission(Community community, SiteUser user) {
        boolean isAuthor = community.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("권한이 없습니다.");
        }
    }

}