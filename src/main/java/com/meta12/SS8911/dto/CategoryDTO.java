package com.meta12.SS8911.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CategoryDTO {

    private Long id;
    private String title;
    private String instructor;
    private String description;

    private MultipartFile attachFile;
    private String fileName;
    private String fileOrigin;

    private Integer orderNum;
}
