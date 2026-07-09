package com.meta12.SS8911.config;

import com.meta12.SS8911.service.ChatPresenceService;
import com.meta12.SS8911.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket(STOMP) 연결이 끊길 때(브라우저 종료, 새로고침, 네트워크 끊김 등)
 * 자동으로 호출됨. 클라이언트가 명시적으로 "나가기" 버튼을 안 눌러도 동작하므로
 * 인원수 카운트가 꼬이지 않게 해줌.
 */
@Component
@RequiredArgsConstructor
public class ChatWebSocketEventListener {

    private final ChatPresenceService chatPresenceService;
    private final ChatService chatService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long roomId = chatPresenceService.leave(sessionId);
        if (roomId != null) {
            chatService.clearRoomIfEmpty(roomId);
        }
    }
}