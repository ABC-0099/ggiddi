package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.AiTutorRequestDTO;
import com.meta12.SS8911.service.AiTutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-tutor")
public class AiTutorController {

    private final AiTutorService aiTutorService;

    @PostMapping
    public ResponseEntity<Map<String, String>> ask(@RequestBody AiTutorRequestDTO dto) {
        String reply = aiTutorService.getReply(dto);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}