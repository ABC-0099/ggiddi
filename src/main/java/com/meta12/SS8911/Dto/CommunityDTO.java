package com.meta12.SS8911.Dto;

import com.meta12.SS8911.config.Category;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommunityDTO {
    private String title;
    private String content;
    private Category category;
}