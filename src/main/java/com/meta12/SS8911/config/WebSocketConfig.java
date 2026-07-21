package com.meta12.SS8911.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 처음 접속하는 엔드포인트 (SockJS fallback 지원)
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독(수신)하는 prefix
        // + heartbeat 설정: 서버-클라이언트 간 10초 간격으로 생존 확인
        //   (탭을 그냥 닫는 등 DISCONNECT 프레임 없이 끊기는 경우를 빠르게 감지하기 위함)
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler());
        // 클라이언트가 메시지를 보낼 때(발행) 붙는 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("chat-cleanup-");
        scheduler.setErrorHandler(throwable ->
                throwable.printStackTrace() // 실제로는 log.error(...)로 교체 권장
        );
        scheduler.initialize();
        return scheduler;
    }
}