package com.meta12.SS8911.controller;

import com.meta12.SS8911.Dto.ChatMessageViewDTO;
import com.meta12.SS8911.Dto.ChatRoomViewDTO;
import com.meta12.SS8911.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatService chatService;

    /**
     * 채팅창을 열 때 보여줄 로비 목록. 모든 로비가 가득 찼다면 새 로비를 자동 생성해서 함께 반환.
     */
    @GetMapping("/api/chat/rooms")
    public List<ChatRoomViewDTO> getRooms() {
        return chatService.getRoomListWithAutoExpand();
    }

    /**
     * 특정 로비의 최근 메시지 N개를 오래된 순으로 반환.
     */
    @GetMapping("/api/chat/rooms/{roomId}/history")
    public List<ChatMessageViewDTO> getHistory(@PathVariable Long roomId,
                                               @RequestParam(defaultValue = "50") int limit) {
        return chatService.getRecentMessages(roomId, limit);
    }
}