package com.meta12.SS8911.controller;

import jakarta.servlet.http.HttpSession;
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

    // 대화 히스토리를 세션에 저장할 때 쓰는 키
    private static final String HISTORY_KEY = "kk_chat_history";

    // 세션당 보관할 최대 메시지 쌍 개수 (너무 길어지면 토큰 낭비되니 최근 N개만 유지)
    private static final int MAX_HISTORY_TURNS = 10;

    // 한자(중국어/일본어 한자) 검출용 정규식
    // CJK 통합 한자 + 확장 A영역을 커버. 한글(가-힣, ㄱ-ㅎ, ㅏ-ㅣ)은 이 범위에 안 걸림.
    private static final java.util.regex.Pattern HANJA_PATTERN =
            java.util.regex.Pattern.compile("[\\u4E00-\\u9FFF\\u3400-\\u4DBF]");

    // 끼역이 캐릭터 설정
    // - 성격/말투 규칙 + "고정 프로필"을 같이 줘서, 매번 다른 TMI를 지어내지 않고
    //   항상 같은 배경 정보를 기반으로 대답하도록 함.
    private static final String SYSTEM_PROMPT = """
        너는 '끼역이'라는 이름의 귀여운 AI 대화 친구야. 🐸

        [고정 프로필 - 반드시 이 설정을 기억하고 항상 일관되게 사용해]
        - 좋아하는 가수: k-pop 가수
        - 좋아하는 음식: 막창, 찜닭, 라면, 삼겹살
        - 좋아하는 디저트 : 치즈케이크, 마카롱, 와플, 크로플
        - 취미: 낮잠 자기, 연못에서 물장구치기
        - 사는 곳: 끼역띠귿 학습 연못 (설정상 캐릭터의 집)
        - 성격: 장난기 많고 친근하고 가끔 엉뚱한 말을 해
        - "오늘 뭐 했어?", "TMI 알려줘" 같은 질문을 받으면 위 고정 프로필 안에서만
          소재를 골라 대답해. 매번 새로운 설정을 즉흥적으로 지어내지 마.

        말투: 반말로 친근하게 말해. 짧고 재밌게. 이모지를 적절히 써.

        규칙:
        - 반드시 한국어로만 답변해. 절대 중국어, 일본어 한자를 섞지 마
        - 답변은 2~3문장으로 짧고 유쾌하게
        - 심심이처럼 가볍고 재밌는 대화 톤 유지
        - 상대가 영어로 말하면 영어로 대답
        - 욕설이나 부적절한 내용에는 "그런 말은 끼역이 싫어~ 🐸💦" 라고 거절
        - 가끔 한국 문화, 유행어, 드립도 섞어줘
        - 상대방이 한국어를 배우고 싶다고 하면 간단히 도와줘
        - 이전 대화 내용과 모순되는 말을 하지 마 (이미 말한 설정은 계속 유지)

        중요: 한글과 이모지만 사용해. 漢字나 中文은 절대 사용 금지.
        """;

    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request, HttpSession session) {
        String userMessage = request.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "메시지를 입력해주세요."));
        }

        try {
            // 세션에서 기존 대화 히스토리 가져오기 (없으면 새로 생성)
            List<Map<String, String>> history =
                    (List<Map<String, String>>) session.getAttribute(HISTORY_KEY);
            if (history == null) {
                history = new ArrayList<>();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 메시지 목록 구성: system + 이전 히스토리 + 이번 유저 메시지
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.addAll(history);
            messages.add(Map.of("role", "user", "content", userMessage));

            String reply = callGroq(messages, headers);

            // 한자가 섞여 나왔으면 규칙을 더 강하게 리마인드해서 1번 재시도
            if (containsHanja(reply)) {
                List<Map<String, String>> retryMessages = new ArrayList<>(messages);
                retryMessages.add(Map.of("role", "assistant", "content", reply));
                retryMessages.add(Map.of(
                        "role", "user",
                        "content", "방금 답변에 한자가 섞여 있었어. 한자(漢字)나 중국어(中文)를 " +
                                "절대 쓰지 말고, 한글과 이모지만 사용해서 같은 내용을 다시 대답해줘."
                ));
                String retryReply = callGroq(retryMessages, headers);
                if (!containsHanja(retryReply)) {
                    reply = retryReply;
                } else {
                    // 재시도에도 한자가 남아있으면 최종 방어선으로 한자만 걸러냄
                    reply = stripHanja(retryReply);
                }
            }

            // 이번 턴을 히스토리에 추가하고, 너무 길어지면 오래된 것부터 잘라냄
            history.add(Map.of("role", "user", "content", userMessage));
            history.add(Map.of("role", "assistant", "content", reply));

            int maxMessages = MAX_HISTORY_TURNS * 2;
            if (history.size() > maxMessages) {
                history = new ArrayList<>(history.subList(history.size() - maxMessages, history.size()));
            }
            session.setAttribute(HISTORY_KEY, history);

            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("reply", "앗, 끼역이가 잠깐 졸았어 😴 다시 해볼래?"));
        }
    }

    // 대화 초기화용 (예: 채팅창 새로 열 때 히스토리 리셋하고 싶으면 프론트에서 호출)
    @PostMapping("/reset")
    public ResponseEntity<?> resetChat(HttpSession session) {
        session.removeAttribute(HISTORY_KEY);
        return ResponseEntity.ok(Map.of("status", "reset"));
    }

    /**
     * Groq API를 호출해서 답변 텍스트만 뽑아 반환.
     */
    @SuppressWarnings("unchecked")
    private String callGroq(List<Map<String, String>> messages, HttpHeaders headers) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("messages", messages);
        body.put("max_tokens", 300);
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GROQ_URL, HttpMethod.POST, entity, Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody != null) {
            List<Map> choices = (List<Map>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map message = (Map) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        return "으엥? 뭔가 잘못됐어 😵 다시 말해줘!";
    }

    /** 응답에 한자(중국어/일본어 한자)가 섞여 있는지 검사 */
    private boolean containsHanja(String text) {
        return text != null && HANJA_PATTERN.matcher(text).find();
    }

    /** 최종 방어선: 한자만 제거하고 공백을 정리 */
    private String stripHanja(String text) {
        if (text == null) return text;
        return HANJA_PATTERN.matcher(text)
                .replaceAll("")
                .replaceAll(" {2,}", " ")
                .trim();
    }
}