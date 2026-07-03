package com.meta12.SS8911.controller;

import com.meta12.SS8911.dto.MockExamResultDTO;
import com.meta12.SS8911.dto.MockExamStartDTO;
import com.meta12.SS8911.dto.MockExamSubmitDTO;
import com.meta12.SS8911.service.MockExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mock")
@RequiredArgsConstructor
public class MockExamApiController {

    private final MockExamService mockExamService;

    @GetMapping("/{examId}/start")
    public MockExamStartDTO start(@PathVariable Long examId) {
        return mockExamService.startExam(examId);
    }

    @PostMapping("/{examId}/submit")
    public MockExamResultDTO submit(@PathVariable Long examId,
                                    @RequestBody MockExamSubmitDTO submitDTO,
                                    Authentication authentication) {
        return mockExamService.submitExam(submitDTO, authentication.getName());
    }
}