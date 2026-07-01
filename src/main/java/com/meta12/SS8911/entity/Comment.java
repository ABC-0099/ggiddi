package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;

    private LocalDateTime createdDate;
}
