package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizFinishRequestDTO {
    private int score;
    private int total;
}