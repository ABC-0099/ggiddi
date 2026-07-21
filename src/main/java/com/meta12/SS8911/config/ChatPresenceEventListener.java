package com.meta12.SS8911.config;

import com.meta12.SS8911.service.ChatPresenceService;
import com.meta12.SS8911.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 채팅 입장/퇴장(SUBSCRIBE/UNSUBSCRIBE/DISCONNECT) 이벤트를 전담하는 단일 리스너.
 * ※ 예전엔 WebSocketEventListener도 같은 disconnect 이벤트를 처리하고 있어서
 *    leave()가 중복 호출되고, 이 클래스의 UNSUBSCRIBE 처리가 먼저 매핑을 지워버려
 *    WebSocketEventListener 쪽에서는 항상 roomId=null을 받아 삭제 스케줄이
 *    아예 등록되지 않는 버그가 있었음. WebSocketEventListener는 제거하고
 *    이 클래스 하나로 입장/퇴장/삭제 스케줄까지 전부 처리하도록 통합함.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatPresenceEventListener {

    private final ChatPresenceService chatPresenceService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;

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
            handleLeave(sessionId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        log.info("[CHAT-PRESENCE] DISCONNECT 이벤트 수신 - sessionId={}", sessionId);
        if (sessionId != null) {
            // 정상적인 leaveRoom() 흐름에서는 UNSUBSCRIBE가 먼저 와서 이미 처리됐을 것이고,
            // 탭을 그냥 닫는 등 UNSUBSCRIBE 없이 바로 끊기는 경우를 위한 안전망으로 여기서도 처리.
            handleLeave(sessionId);
        }
    }

    /**
     * 세션을 방에서 제거하고, 실제로 방을 나간 것이었다면(=roomId가 있었다면)
     * 인원수를 방송하고 1분 뒤 빈 방 정리를 예약한다.
     * UNSUBSCRIBE/DISCONNECT 둘 중 어느 쪽이 먼저 오든 여기서 한 곳에서만 처리하므로
     * leave() 중복 호출로 인한 roomId 유실 문제가 생기지 않는다.
     */
    private void handleLeave(String sessionId) {
        Long roomId = chatPresenceService.leave(sessionId);
        log.info("[CHAT-PRESENCE] leave 처리 결과 - sessionId={}, roomId={}", sessionId, roomId);

        if (roomId == null) {
            // 이미 다른 이벤트에서 처리되어 매핑이 없는 경우 (정상)
            return;
        }

        int count = chatPresenceService.getCount(roomId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/count", count);

        Long finalRoomId = roomId;
        log.info("[CHAT-PRESENCE] roomId={} 1분 뒤 정리 예약", finalRoomId);
        taskScheduler.schedule(
                () -> {
                    log.info("[CHAT-PRESENCE] roomId={} 정리 스케줄 실행", finalRoomId);
                    chatService.clearRoomIfEmpty(finalRoomId);
                },
                Instant.now().plus(1, ChronoUnit.MINUTES)
        );
    }
}