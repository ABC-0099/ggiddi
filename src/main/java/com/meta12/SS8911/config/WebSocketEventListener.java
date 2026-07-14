package com.meta12.SS8911.config;

import com.meta12.SS8911.service.ChatPresenceService;
import com.meta12.SS8911.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatPresenceService chatPresenceService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler; // 아래 Bean 등록 필요

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long roomId = chatPresenceService.leave(sessionId);
        if (roomId != null) {
            broadcastCount(roomId);

            // ★ 즉시 삭제 대신 1분 뒤 재확인 후 삭제
            taskScheduler.schedule(
                    () -> chatService.clearRoomIfEmpty(roomId),
                    Instant.now().plus(1, ChronoUnit.MINUTES)
            );
        }
    }

    public void broadcastCount(Long roomId) {
        int count = chatPresenceService.getCount(roomId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/count", count);
    }
}