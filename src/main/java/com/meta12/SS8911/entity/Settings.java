package com.meta12.SS8911.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String siteName = "끼역띠귿";
    private String siteDescription = "외국인을 위한 한국어 학습 플랫폼";

    private boolean signupEnabled = true;
    private boolean emailVerification = false;
    private boolean rejoinEnabled = true;

    private int boardPageSize = 10;

    private boolean commentEnabled = true;
    private boolean attachmentEnabled = true;
}