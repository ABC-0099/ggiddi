package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockExamListItemDTO {

    private Long id;
    private String title;         // 예: "1회차 모의고사"
    private Integer round;
    private String categoryName;  // 테마명
    private int poolCount;        // 등록된 문항 수 (=출제 문항 수, 전체 다 나옴)
    private int timeLimitMinutes; // 제한시간(분)
    private Integer lastScore;    // 최근 정답률(%) - 안 봤으면 null
    private int attemptCount;     // 누적 응시 횟수
}