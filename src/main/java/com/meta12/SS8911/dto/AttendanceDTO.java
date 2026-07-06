package com.meta12.SS8911.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor    // 기본 생성자 추가
@AllArgsConstructor   // 모든 필드를 받는 생성자 추가 (이게 있어야 new AttendanceDTO(값) 가능!)
public class AttendanceDTO {
    private String date;
}