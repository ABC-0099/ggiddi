package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.AiTutorRequestDTO;
import com.meta12.SS8911.client.GroqTutorClient;
import com.meta12.SS8911.util.DDigeudCharacterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiTutorService {

    private final GroqTutorClient groqTutorClient;

    private static final String SYSTEM_PROMPT = """
        너는 '띠귿이'라는 이름의 AI 튜터야. 부엉이 캐릭터고, 차분하고 정중한 존댓말(해요체)로 말해.
        끼역이(아이돌 팬 톤의 친근한 개구리 캐릭터)와는 다르게, 너는 학습 보조에만 집중하는 과외 선생님 같은 톤을 유지해.
        잡담이나 아이돌/음식 얘기는 하지 않고, 강의 내용과 학습자의 질문에만 집중해서 정확하고 간결하게 설명해.
        답변은 한국어 학습자가 이해하기 쉬운 한글 위주로 작성하고, 이모지는 사용하지 않아.
        답변은 반드시 2~3문장 이내로 짧게 작성해. 배경 설명이나 예시를 길게 늘어놓지 말고 핵심만 말해.
        학습자가 더 자세히 알고 싶다고 추가로 물어보면 그때 조금 더 설명해도 돼.
        """;

    public String getReply(AiTutorRequestDTO dto) {
        String userPrompt = buildContext(dto) + "\n\n학습자 질문: " + dto.getQuestion();
        String rawReply = groqTutorClient.chat(SYSTEM_PROMPT, userPrompt);
        return DDigeudCharacterFilter.filter(rawReply);
    }

    private String buildContext(AiTutorRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("[강의 제목] ").append(dto.getLectureTitle()).append("\n");
        if (dto.getLectureContent() != null && !dto.getLectureContent().isBlank()) {
            sb.append("[강의 내용] ").append(dto.getLectureContent()).append("\n");
        }
        if (dto.getVideoTimestamp() != null) {
            int min = dto.getVideoTimestamp() / 60;
            int sec = dto.getVideoTimestamp() % 60;
            sb.append(String.format("[학습자가 보고 있는 시점] %d분 %d초 지점", min, sec));
        }
        return sb.toString();
    }
}