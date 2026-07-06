package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.TranslateRequestDTO;
import com.meta12.SS8911.service.TranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/translate")
public class TranslateController {

    private final TranslateService translateService;

    @PostMapping
    public ResponseEntity<Map<String, String>> translate(@RequestBody TranslateRequestDTO dto) {
        String translated = translateService.translate(dto);
        return ResponseEntity.ok(Map.of("translatedText", translated));
    }
}