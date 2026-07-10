package com.meta12.SS8911.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ★ SiteUserService 주입 없음 → 순환참조 없음
    // Spring Security가 UserDetailsService 구현체(SiteUserService)를 자동으로 찾아서 씀

    // ★★★ 핵심 수정: 정적 리소스는 Security 필터체인 자체를 안 타게 함
    //   permitAll()은 "인증"만 건너뛸 뿐, CsrfFilter 등 다른 필터는 여전히 실행됨.
    //   CsrfTokenRequestAttributeHandler(비-Xor)는 요청마다 토큰을 즉시(eager) resolve해서
    //   쿠키를 새로 저장하는데, 페이지 로드시 css/js/font/favicon이 동시다발적으로
    //   요청되면서 서로 다른 랜덤 토큰을 발급받아 쿠키를 덮어써버림 → 폼에 찍힌 _csrf 값과
    //   실제 쿠키 값이 달라져서 로그인 폼 제출 시 403(CSRF 불일치)이 나는 문제의 근본 원인.
    //   web.ignoring()으로 아예 필터체인 밖으로 빼면 이 레이스 컨디션이 사라짐.

    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/images/**",
                "/fonts/**",
                "/favicon.ico",
                "/games/**"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // ★ eager resolve 끄기: 요청마다 CSRF 토큰을 즉시 재계산해서 쿠키(XSRF-TOKEN)를
        //   갈아치우는 레이스 컨디션을 방지. 실제로 값이 필요할 때(Thymeleaf가 _csrf.token을
        //   렌더링할 때)만 지연 계산하도록 함 → 폼의 _csrf 값과 쿠키 값이 어긋나는 문제 해결.
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/siteUser/login",
                                "/siteUser/chuga",
                                "/siteUser/chugaProc",
                                "/api/check-username",   // ★ 추가: 회원가입 아이디 중복확인 (비로그인 접근 가능해야 함)
                                "/api/check-phone",      // ★ 추가: 회원가입 전화번호 중복확인 (비로그인 접근 가능해야 함)
                                "/notices",
                                "/faq",
                                "/lectures",
                                "/lectures/**",
                                "/game",
                                "/games/**",
                                "/practice/main",
                                "/practice/mock",
                                "/qna/main",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/ws/chat/**",
                                "/api/chat/**",
                                "/error",
                                "/favicon.ico",
                                "/.well-known/**",
                                "/api/ai-tutor/**",
                                "/api/translate/**"
                        ).permitAll()
                        .requestMatchers("/quiz/**", "/api/quiz/**").authenticated()
                        .requestMatchers("/admin/quiz/**", "/api/admin/quiz/**").authenticated()
                        .requestMatchers("/api/ai-chat").authenticated()
                        .requestMatchers("/community/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/siteUser/login")
                        .loginProcessingUrl("/siteUser/login")
                        .successHandler(loginSuccessHandler)
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
                        // ★ 세션 대신 쿠키(XSRF-TOKEN)에 CSRF 토큰 저장
                        //   → 큰 페이지 렌더링 중간에 응답이 커밋된 후 세션을 새로 만들려다
                        //     발생하는 IllegalStateException("Cannot create a session after
                        //     the response has been committed") 를 근본적으로 방지
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // ★ 기본 XorCsrfTokenRequestAttributeHandler는 요청 헤더값이 XOR 마스킹되어
                        //   있을 것으로 기대함. 근데 JS는 쿠키(XSRF-TOKEN)의 원본 값을 그대로
                        //   헤더에 실어 보내므로, 마스킹 없이 원본 그대로 비교하는 핸들러로 명시.
                        //   (이거 안 하면 CookieCsrfTokenRepository + JS fetch 조합에서 403 남)
                        .csrfTokenRequestHandler(requestHandler)
                )
                .headers(headers -> headers
                        // ★ 기본값 DENY는 iframe(유니티 게임 창)을 전부 막으므로 같은 출처는 허용하도록 변경
                        .frameOptions(frame -> frame.sameOrigin())
                );
        // ★ csrf.disable() 제거 → CSRF 기본 활성화 (다른 요청에는 그대로 적용됨)

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}