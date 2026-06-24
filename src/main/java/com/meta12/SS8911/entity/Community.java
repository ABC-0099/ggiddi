package com.meta12.SS8911.entity;

import com.meta12.SS8911.controller.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.Locale;

@Entity
@Getter @Setter
public class Community {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private Category category; // CULTURAL, REVIEW, FREE

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author; // 작성자 정보

    // 생성일, 수정일 등...
}