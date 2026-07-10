package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.ChatMessageDTO;
import com.meta12.SS8911.dto.ChatMessageViewDTO;
import com.meta12.SS8911.service.ChatPresenceService;
import com.meta12.SS8911.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatPresenceService chatPresenceService;

    /**
     * 클라이언트가 구독을 시작한 직후 "/app/chat/{roomId}/enter"로 호출해서
     * 이 세션이 해당 방에 입장했음을 서버 메모리(ChatPresenceService)에 등록.
     * 이걸 호출해야 로비 목록의 인원수가 올라감.
     */
    @MessageMapping("/chat/{roomId}/enter")
    public void enterRoom(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor, Authentication authentication) {
        if (authentication == null) {
            return;
        }
        String sessionId = accessor.getSessionId();
        chatPresenceService.join(roomId, sessionId);

        // ★ 입장 직후 최신 인원수를 이 방을 구독 중인 모두에게 방송 (실시간 반영의 핵심)
        int count = chatPresenceService.getCount(roomId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/count", count);
    }

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