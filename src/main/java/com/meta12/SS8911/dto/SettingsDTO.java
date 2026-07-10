package com.meta12.SS8911.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsDTO {
    private String siteName;
    private String siteDescription;
    private boolean signupEnabled;
    private boolean emailVerification;
    private boolean rejoinEnabled;
    private int boardPageSize;
    private boolean commentEnabled;
    private boolean attachmentEnabled;
}