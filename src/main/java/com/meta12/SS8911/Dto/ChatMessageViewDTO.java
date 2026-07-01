package com.meta12.SS8911.Dto;

import com.meta12.SS8911.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageViewDTO {

    private Long id;
    private Long roomId;
    private String username;
    private String content;
    private String time; // HH:mm 형태로 가공해서 내려줌

    public static ChatMessageViewDTO from(ChatMessage message) {
        return ChatMessageViewDTO.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .username(message.getAuthor().getUsername())
                .content(message.getContent())
                .time(message.getCreatedDate().format(DateTimeFormatter.ofPattern("HH:mm")))
                .build();
    }
}