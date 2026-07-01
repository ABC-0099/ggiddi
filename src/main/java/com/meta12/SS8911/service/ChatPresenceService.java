package com.meta12.SS8911.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 채팅 로비별 실시간 접속 인원을 메모리에서 추적.
 * 서버 재시작 시 초기화되지만, 학습 프로젝트 규모에서는 충분함.
 * (운영급으로 가려면 Redis 등 외부 저장소로 옮겨야 함)
 */
@Service
public class ChatPresenceService {

    // roomId -> 그 방에 입장한 WebSocket 세션 ID 목록
    private final Map<Long, Set<String>> roomSessions = new ConcurrentHashMap<>();

    // sessionId -> 현재 입장해 있는 roomId (한 세션은 한 번에 한 방만 구독한다고 가정)
    private final Map<String, Long> sessionRoom = new ConcurrentHashMap<>();

    public void join(Long roomId, String sessionId) {
        // 기존에 다른 방에 있었다면 먼저 빼줌
        leave(sessionId);

        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionRoom.put(sessionId, roomId);
    }

    public void leave(String sessionId) {
        Long roomId = sessionRoom.remove(sessionId);
        if (roomId != null) {
            Set<String> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(sessionId);
            }
        }
    }

    public int getCount(Long roomId) {
        Set<String> sessions = roomSessions.get(roomId);
        return sessions == null ? 0 : sessions.size();
    }
}