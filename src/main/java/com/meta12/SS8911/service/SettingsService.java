package com.meta12.SS8911.service;

import com.meta12.SS8911.entity.Settings;
import com.meta12.SS8911.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository repository;

    public Settings getSettings() {

        return repository.findById(1L)
                .orElseGet(() -> {

                    Settings settings = new Settings();

                    settings.setSiteName("끼역띠귿");
                    settings.setSiteDescription("한국어 학습 플랫폼");

                    settings.setSignupEnabled(true);
                    settings.setEmailVerification(true);
                    settings.setRejoinEnabled(false);

                    settings.setBoardPageSize(10);

                    settings.setCommentEnabled(true);
                    settings.setAttachmentEnabled(true);

                    return repository.save(settings);
                });
    }

    public void save(Settings settings) {

        settings.setId(1L);

        repository.save(settings);
    }
}