package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class QuizUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;  // 문항 내용 (예: "밥"의 뜻으로 올바른 것은?)
    private int unitOrder;   // 문항 순서 (1, 2, 3...)

    private String optionA;  // 보기 A
    private String optionB;  // 보기 B
    private String optionC;  // 보기 C
    private String optionD;  // 보기 D
    private String answer;   // 정답 ("A", "B", "C", "D")

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;
}
