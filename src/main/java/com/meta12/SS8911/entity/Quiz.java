package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 퀴즈 제목 (예: "2-2강 퀴즈")

    // 나중에 강의 연동 시 아래 주석 해제
    // @ManyToOne(fetch = FetchType.LAZY)
    // private Lecture lecture;

    private boolean unlocked = false; // 잠금 여부 (강의 완료 시 true)

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizUnit> units = new ArrayList<>();
}
