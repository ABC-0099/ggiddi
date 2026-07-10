package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.SettingsDTO;
import com.meta12.SS8911.entity.Settings;
import com.meta12.SS8911.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    // 설정은 row 1개만 사용. 없으면 기본값으로 생성해서 저장 후 반환.
    @Transactional
    public Settings get() {
        return settingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> settingsRepository.save(new Settings()));
    }

    @Transactional
    public void save(SettingsDTO dto) {
        Settings settings = get(); // 기존 row 가져오기 (없으면 생성)

        settings.setSiteName(dto.getSiteName());
        settings.setSiteDescription(dto.getSiteDescription());
        settings.setSignupEnabled(dto.isSignupEnabled());
        settings.setEmailVerification(dto.isEmailVerification());
        settings.setRejoinEnabled(dto.isRejoinEnabled());
        settings.setBoardPageSize(dto.getBoardPageSize());
        settings.setCommentEnabled(dto.isCommentEnabled());
        settings.setAttachmentEnabled(dto.isAttachmentEnabled());

        settingsRepository.save(settings);
    }
}