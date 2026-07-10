package com.meta12.SS8911.config;

import com.meta12.SS8911.service.ChatPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 브라우저 탭을 닫거나 페이지를 이탈해서 WebSocket 연결이 끊기면
 * Spring이 SessionDisconnectEvent를 발행함. 이걸 받아서
 * 1) ChatPresenceService에서 해당 세션을 방에서 제거하고
 * 2) 남아있는 사람들에게 최신 인원수를 방송함.
 * (이 리스너가 없으면 나간 사람이 인원수에서 영원히 안 빠짐)
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatPresenceService chatPresenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long roomId = chatPresenceService.leave(sessionId);
        if (roomId != null) {
            broadcastCount(roomId);
        }
    }

    public void broadcastCount(Long roomId) {
        int count = chatPresenceService.getCount(roomId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/count", count);
    }
}