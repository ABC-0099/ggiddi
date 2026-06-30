package com.meta12.SS8911.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler; // 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/siteUser/login",
                                "/siteUser/chuga",
                                "/siteUser/chugaProc",
                                "/notices",
                                "/faq",
                                "/lectures",
                                "/lectures/**",
                                "/game",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/ws/chat/**",
                                "/api/chat/history"
                        ).permitAll()
                        // /community/** 는 위 목록에서 제외 → anyRequest().authenticated()에 걸려 로그인 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/siteUser/login")
                        .loginProcessingUrl("/siteUser/login")
                        .successHandler(loginSuccessHandler) // 추가
                        .failureUrl("/siteUser/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        // WebSocket 핸드셰이크는 STOMP 프레임 자체로 인증되므로 CSRF 토큰 검사에서 제외
                        .ignoringRequestMatchers("/ws/chat/**")
                );
// ★ csrf.disable() 제거 → CSRF 기본 활성화 (다른 요청에는 그대로 적용됨)

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}