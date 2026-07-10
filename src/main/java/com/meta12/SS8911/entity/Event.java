package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // 이벤트 제목
    private String title;


    // 이벤트 내용
    @Column(columnDefinition = "TEXT")
    private String content;


    // 이벤트 시작일
    private LocalDate startDate;


    // 이벤트 종료일
    private LocalDate endDate;


    // 이벤트 상태
    // UPCOMING / ONGOING / ENDED
    private String status;


    // 상세 페이지 포스터 이미지 - 이제 Cloudinary URL이 통째로 들어감 → length 넉넉하게
    @Column(length = 500)
    private String poster;


    // 목록 썸네일 이미지 - 이제 Cloudinary URL이 통째로 들어감 → length 넉넉하게
    @Column(length = 500)
    private String thumbnail;


    // 등록일
    private LocalDate createdDate = LocalDate.now();


    // 참여자 수
    private Integer participantCount = 0;

}