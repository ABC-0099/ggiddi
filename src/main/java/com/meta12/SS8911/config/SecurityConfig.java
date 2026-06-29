package com.meta12.SS8911.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ★ SiteUserService 주입 없음 → 순환참조 없음
    // Spring Security가 UserDetailsService 구현체(SiteUserService)를 자동으로 찾아서 씀

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
                                "/community/**",
                                "/faq",
                                "/lectures",
                                "/lectures/**",
                                "/game",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/siteUser/login")
                        .loginProcessingUrl("/siteUser/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/siteUser/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        // ★ csrf.disable() 제거 → CSRF 기본 활성화

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}