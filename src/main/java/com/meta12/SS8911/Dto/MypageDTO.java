package com.meta12.SS8911.Dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class MypageDTO {
    private String username;
    private String email;
    private List<CommunityDTO> myPosts; // 내가 쓴 글 목록
}