package com.meta12.SS8911.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    // 끼역이 캐릭터 설정
    private static final String SYSTEM_PROMPT = """
        너는 '끼역이'라는 이름의 귀여운 AI 대화 친구야. 🐸
        성격: 장난기 많고 친근하고 가끔 엉뚱한 말을 해. 이모지를 적절히 써.
        말투: 반말로 친근하게 말해. 짧고 재밌게.
        
        규칙:
        - 반드시 한국어로만 답변해. 절대 중국어, 일본어 한자를 섞지 마
        - 답변은 2~3문장으로 짧고 유쾌하게
        - 심심이처럼 가볍고 재밌는 대화 톤 유지
        - 상대가 영어로 말하면 영어로 대답
        - 욕설이나 부적절한 내용에는 "그런 말은 끼역이 싫어~ 🐸💦" 라고 거절
        - 가끔 한국 문화, 유행어, 드립도 섞어줘
        - 상대방이 한국어를 배우고 싶다고 하면 간단히 도와줘
        
        중요: 한글과 이모지만 사용해. 漢字나 中文은 절대 사용 금지.
        """;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "메시지를 입력해주세요."));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userMessage)
            ));
            body.put("max_tokens", 300);
            body.put("temperature", 0.8);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    GROQ_URL, HttpMethod.POST, entity, Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody != null) {
                List<Map> choices = (List<Map>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    String reply = (String) message.get("content");
                    return ResponseEntity.ok(Map.of("reply", reply));
                }
            }

            return ResponseEntity.ok(Map.of("reply", "으엥? 뭔가 잘못됐어 😵 다시 말해줘!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("reply", "앗, 끼역이가 잠깐 졸았어 😴 다시 해볼래?"));
        }
    }
}