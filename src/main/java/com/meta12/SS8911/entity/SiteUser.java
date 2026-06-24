package com.meta12.SS8911.entity;

import com.meta12.SS8911.config.Role; // 1. 추가한 Role Enum import
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    // 2. 권한 필드 추가
    @Enumerated(EnumType.STRING)
    private Role role;
}