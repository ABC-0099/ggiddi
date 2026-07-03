package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizListItemDTO {

    private Long id;
    private String title;         // 퀴즈 세트 제목 (예: "1강 연습퀴즈")
    private String contentTitle;  // 소속 강의명 (배지에 표시)
    private String categoryName;  // 소속 테마명 (예: "케이팝")
    private int poolCount;        // 문제은행 전체 문항 수
    private int questionCount;    // 매 시도마다 뽑는 문항 수
    private Integer lastScore;    // 최근 정답률(%) - 안 풀었으면 null
    private boolean unlocked;     // 영상 시청 + 이전 차시 완료 여부
}