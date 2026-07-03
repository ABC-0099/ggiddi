package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_exam_box")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockExamBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private MockExam mockExam;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser user;

    private int score;           // 맞은 개수
    private int total;           // 전체 문항 수
    private Integer elapsedSeconds; // 실제 소요 시간(초) - 선택
    private LocalDateTime solvedDate;
}