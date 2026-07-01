package com.meta12.SS8911.service;

import com.meta12.SS8911.Dto.ChatMessageViewDTO;
import com.meta12.SS8911.Dto.ChatRoomViewDTO;
import com.meta12.SS8911.entity.ChatMessage;
import com.meta12.SS8911.entity.ChatRoom;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.ChatMessageRepository;
import com.meta12.SS8911.repository.ChatRoomRepository;
import com.meta12.SS8911.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int ROOM_CAPACITY = 30;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SiteUserRepository siteUserRepository;
    private final ChatPresenceService chatPresenceService;

    /**
     * 전체 로비 목록을 현재 인원수와 함께 반환.
     * 로비가 하나도 없으면 "로비 1"을 자동 생성.
     */
    @Transactional
    public List<ChatRoomViewDTO> getRoomList() {
        List<ChatRoom> rooms = chatRoomRepository.findAllByOrderByIdAsc();

        if (rooms.isEmpty()) {
            ChatRoom firstRoom = chatRoomRepository.save(new ChatRoom("로비 1", ROOM_CAPACITY));
            rooms = List.of(firstRoom);
        }

        return rooms.stream()
                .map(room -> toViewDTO(room))
                .collect(Collectors.toList());
    }

    /**
     * 모든 로비가 가득 찼다면 새 로비를 만들어서 목록에 추가.
     * (입장 화면을 새로고침했을 때 호출하면 됨)
     */
    @Transactional
    public List<ChatRoomViewDTO> getRoomListWithAutoExpand() {
        List<ChatRoomViewDTO> rooms = getRoomList();

        boolean allFull = rooms.stream().allMatch(ChatRoomViewDTO::isFull);
        if (allFull) {
            int nextNumber = rooms.size() + 1;
            ChatRoom newRoom = chatRoomRepository.save(new ChatRoom("로비 " + nextNumber, ROOM_CAPACITY));
            rooms = new java.util.ArrayList<>(rooms);
            rooms.add(toViewDTO(newRoom));
        }

        return rooms;
    }

    private ChatRoomViewDTO toViewDTO(ChatRoom room) {
        int count = chatPresenceService.getCount(room.getId());
        return ChatRoomViewDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .currentCount(count)
                .capacity(room.getCapacity())
                .full(count >= room.getCapacity())
                .build();
    }

    /**
     * 메시지를 저장하고, broadcast에 바로 쓸 수 있는 ViewDTO로 변환해 반환.
     */
    @Transactional
    public ChatMessageViewDTO saveMessage(Long roomId, String username, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        SiteUser author = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .room(room)
                        .author(author)
                        .content(content.trim())
                        .build()
        );

        return ChatMessageViewDTO.from(saved);
    }

    /**
     * 특정 방의 최근 메시지 N개를 오래된 순으로 정렬해서 반환.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageViewDTO> getRecentMessages(Long roomId, int limit) {
        List<ChatMessage> recent = chatMessageRepository
                .findAllByRoomIdOrderByCreatedDateDesc(roomId, PageRequest.of(0, limit));
        Collections.reverse(recent); // 최신순 -> 오래된순으로 뒤집기
        return recent.stream().map(ChatMessageViewDTO::from).collect(Collectors.toList());
    }
}