package com.meta12.SS8911.config;

import com.meta12.SS8911.service.ChatPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatPresenceEventListener {

    private final ChatPresenceService chatPresenceService;

    // "/topic/chat/{roomId}" 형태의 구독 경로에서 roomId 추출
    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("^/topic/chat/(\\d+)$");

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();

        log.info("[CHAT-PRESENCE] SUBSCRIBE 이벤트 수신 - destination={}, sessionId={}", destination, sessionId);

        if (destination == null || sessionId == null) {
            log.warn("[CHAT-PRESENCE] destination 또는 sessionId가 null이라 무시함");
            return;
        }

        Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long roomId = Long.valueOf(matcher.group(1));
            chatPresenceService.join(roomId, sessionId);
            log.info("[CHAT-PRESENCE] roomId={}에 sessionId={} 입장 처리, 현재 인원={}",
                    roomId, sessionId, chatPresenceService.getCount(roomId));
        } else {
            log.info("[CHAT-PRESENCE] destination이 채팅방 패턴과 매칭되지 않음: {}", destination);
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        log.info("[CHAT-PRESENCE] UNSUBSCRIBE 이벤트 수신 - sessionId={}", sessionId);
        if (sessionId != null) {
            chatPresenceService.leave(sessionId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        log.info("[CHAT-PRESENCE] DISCONNECT 이벤트 수신 - sessionId={}", sessionId);
        if (sessionId != null) {
            chatPresenceService.leave(sessionId);
        }
    }
}