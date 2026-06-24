package com.meta12.SS8911.service;

import com.meta12.SS8911.entity.Community;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.MypageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final MypageRepository mypageRepository;

    public List<Community> getPostsByAuthor(SiteUser author) {
        return mypageRepository.findByAuthorOrderByCreatedDateDesc(author);
    }
}