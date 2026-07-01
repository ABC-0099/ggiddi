package com.meta12.SS8911.entity;

import com.meta12.SS8911.config.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class SiteUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String name;
    private String birth;
    private String phone;
    private String email;
    private String password;
    private String nationality;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 연속 출석 관련 필드
    private int streakDays;              // 연속 출석일수
    private LocalDateTime lastLoginDate; // 마지막 로그인 시각

    private LocalDateTime joinDate;      // 가입일
}