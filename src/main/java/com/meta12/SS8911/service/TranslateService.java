package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.TranslateRequestDTO;
import com.meta12.SS8911.client.GroqTutorClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final GroqTutorClient groqTutorClient;

    private static final Map<String, String> LANG_NAMES = Map.of(
            "en", "English",
            "zh", "Chinese (Simplified)",
            "ja", "Japanese",
            "vi", "Vietnamese",
            "tl", "Filipino (Tagalog)",
            "id", "Indonesian",
            "th", "Thai"
    );

    private static final String SYSTEM_PROMPT = """
        You are a translation engine. Translate the given Korean text into the requested target language.
        Output ONLY the translated text. Do not add explanations, notes, quotation marks, or any extra text.
        Keep the tone natural and appropriate for a language-learning app aimed at foreign learners of Korean.
        """;

    public String translate(TranslateRequestDTO dto) {
        if (dto.getText() == null || dto.getText().isBlank()) return "";

        String targetLangName = LANG_NAMES.getOrDefault(dto.getTargetLang(), dto.getTargetLang());
        String userPrompt = "Target language: " + targetLangName + "\n\nText:\n" + dto.getText();

        String result = groqTutorClient.chat(SYSTEM_PROMPT, userPrompt);
        return (result == null || result.isBlank()) ? "번역에 실패했어요. 잠시 후 다시 시도해주세요." : result.trim();
    }
}