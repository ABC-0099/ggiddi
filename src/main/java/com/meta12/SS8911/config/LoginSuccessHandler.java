package com.meta12.SS8911.config;

import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.SiteUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SiteUserRepository siteUserRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = user.getLastLoginDate();

        if (last == null) {
            // 첫 로그인
            user.setStreakDays(1);
        } else if (Duration.between(last, now).toHours() <= 24) {
            // 24시간 이내 재로그인 → 누적
            user.setStreakDays(user.getStreakDays() + 1);
        } else {
            // 24시간 초과 → 리셋
            user.setStreakDays(1);
        }

        user.setLastLoginDate(now);
        siteUserRepository.save(user);

        response.sendRedirect("/");
    }
}
