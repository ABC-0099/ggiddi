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

@Configuration
@EnableWebSecurity
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
                                "/notices",
                                "/faq",
                                "/lectures",
                                "/lectures/**",
                                "/game",
                                "/games/**",         // ★ 유니티 WebGL 빌드 정적 파일 인증 없이 접근 허용
                                "/practice/main",
                                "/practice/mock",     // ★ 배움터 메인/모의고사 메인은 비로그인도 열람 가능
                                "/qna/main",         // ★ 질문센터 메인(FAQ+1:1 문의 카드)은 비로그인도 열람 가능
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/ws/chat/**",
                                "/api/chat/**",
                                "/error",            // ★ 에러 페이지가 인증 필요 경로로 잡히면서 CSRF 토큰이 새로 갈아치워지는 문제 방지
                                "/favicon.ico",       // ★ 파비콘 요청도 인증 체크에서 제외
                                "/.well-known/**"   // ★ 크롬 devtools 자동 요청 무시용
                        ).permitAll()
                        // ★ 연습퀴즈 풀이/제출은 로그인 필요 (QuizService가 로그인 유저 기준으로 채점·잠금체크함)
                        .requestMatchers("/quiz/**", "/api/quiz/**").authenticated()
                        // ★ 연습퀴즈 관리자 CRUD - 로그인 필요 (역할 체크는 컨트롤러/서비스 단에서 추가로 확인 권장)
                        .requestMatchers("/admin/quiz/**", "/api/admin/quiz/**").authenticated()
                        // ★ 커뮤니티는 로그인한 회원(및 관리자)만 열람 가능하도록 명시적으로 인증 필요 처리
                        .requestMatchers("/api/ai-chat").authenticated()
                        .requestMatchers("/community/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/siteUser/login")
                        .loginProcessingUrl("/siteUser/login")
                        .defaultSuccessUrl("/", true)
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