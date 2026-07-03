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

    private int streakDays;
    private LocalDateTime lastLoginDate;
    private LocalDateTime joinDate;

    // ★ 추가: 탈퇴 관련 필드
    private boolean withdrawn;
    private LocalDateTime withdrawnDate;

    // ★ 추가: 화면에 표시할 이름 (탈퇴 회원이면 "탈퇴한 회원"으로 표시)
    public String getDisplayName() {
        return withdrawn ? "탈퇴한 회원" : username;
    }
}