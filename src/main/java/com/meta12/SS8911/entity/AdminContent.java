package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;  // 💡 추가
import lombok.Setter;  // 💡 추가
import java.time.LocalDateTime;

@Entity
@Getter  // 💡 컨트롤러에서 ac.getId(), ac.getTitle() 등을 쓸 수 있게 해줍니다!
@Setter  // 💡 값을 넣을 때 필요합니다.
public class AdminContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String step;

    private int lectureCount;

    private String status;

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
}