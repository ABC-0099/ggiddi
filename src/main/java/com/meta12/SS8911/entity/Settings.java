package com.meta12.SS8911.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Settings {

    @Id
    private Long id = 1L;

    private String siteName;
    private String siteDescription;

    private boolean signupEnabled;
    private boolean emailVerification;
    private boolean rejoinEnabled;

    private int boardPageSize;

    private boolean commentEnabled;
    private boolean attachmentEnabled;
}