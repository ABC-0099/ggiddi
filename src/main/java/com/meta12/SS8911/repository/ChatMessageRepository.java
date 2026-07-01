package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 방의 최근 메시지 N개를 최신순으로 조회 (입장 시 이전 대화 보여주기용)
    List<ChatMessage> findAllByRoomIdOrderByCreatedDateDesc(Long roomId, Pageable pageable);
}