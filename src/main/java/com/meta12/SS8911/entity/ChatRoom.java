package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // "로비 1", "로비 2" ...

    @Column(nullable = false)
    private int capacity = 30; // 한 로비 최대 인원

    public ChatRoom(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }
}