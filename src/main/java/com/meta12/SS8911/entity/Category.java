package com.meta12.SS8911.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Category {

    //영상 카테고리
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;       //강좌제목
    private String instructor;  //강사명
    private String description; //강좌설명

//    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE, orphanRemoval = true)
//    private List<Content> contents;

    private String fileName;
    private String fileOrigin;

//    @OneToMany(mappedBy = "category")
//    @JsonIgnore // 💡 이 어노테이션을 추가하세요!
//    private List<Content> contentList;
//
}
