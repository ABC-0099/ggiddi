package com.meta12.SS8911.controller;

import com.meta12.SS8911.Dto.ChatMessageDTO;
import com.meta12.SS8911.Dto.ChatMessageViewDTO;
import com.meta12.SS8911.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * 클라이언트가 "/app/chat/{roomId}/send"로 메시지를 보내면 호출됨.
     * DB에 저장 후 "/topic/chat/{roomId}"를 구독 중인 같은 방 클라이언트에게만 broadcast.
     */
    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDTO dto, Authentication authentication) {

        if (authentication == null || dto.getContent() == null || dto.getContent().isBlank()) {
            return;
        }

        ChatMessageViewDTO saved = chatService.saveMessage(roomId, authentication.getName(), dto.getContent());

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, saved);
    }
}