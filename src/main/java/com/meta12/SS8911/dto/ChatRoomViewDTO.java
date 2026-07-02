package com.meta12.SS8911.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomViewDTO {
    private Long id;
    private String name;
    private int currentCount;
    private int capacity;
    private boolean full;
}